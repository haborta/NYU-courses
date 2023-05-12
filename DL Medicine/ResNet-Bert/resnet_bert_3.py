####### Load libraries #######
import pandas as pd
import torch
import numpy as np
from PIL import Image
from torch.utils.data import Dataset, DataLoader
from torchvision.transforms import Compose, Resize, CenterCrop
from transformers import BertTokenizer, BertModel, AdamW, BertForSequenceClassification
from torchvision import transforms
from transformers import AutoTokenizer, AutoImageProcessor
import re
import string
import torch.nn as nn
import torchvision.models as models
from transformers import BertModel
#from skimage import io
#from skimage import color
import torch.optim as optim
from sklearn.metrics import f1_score, roc_auc_score


###### Device ######
if torch.cuda.is_available():      
    device = torch.device("cuda")
    print('There are %d GPU(s) available.' % torch.cuda.device_count())
    print('We will use the GPU:', torch.cuda.get_device_name(0))
else:
    print('No GPU available, using the CPU instead.')
    device = torch.device("cpu")


###### Working directory ######
wd = ""
import os
os.chdir(wd)
print(f"Working dir: {wd}")


###### Random seed ######
import torch
import numpy as np
import random 

random_seed = 2023
torch.manual_seed(random_seed)
random.seed(random_seed)
np.random.seed(random_seed)



##### Hyperparameters #######
BATCH_SIZE = 16
epochs = 24
lr = 1e-3
MODEL_NAME = "resnet_bert_3_1.pt"

print(f"Batch size: {BATCH_SIZE}")
print(f"Learning rate: {lr}")
print(f"Number of epochs: {epochs}")
print(f"Model name: {MODEL_NAME}")



###### Data path #######
#train_path = wd+"train_subset.csv"
#valid_path = wd+"valid_subset.csv"
#test_path = wd+"test_subset.csv"

train_path = wd+"train_subset_3.csv"
valid_path = wd+"valid_subset_3.csv"
test_path = wd+"test_subset_3.csv"

train_subset = pd.read_csv(train_path)
valid_subset = pd.read_csv(valid_path)
test_subset = pd.read_csv(test_path)

print(f"Train size: {len(train_subset)}")
print(f"Valid size: {len(valid_subset)}")



###### Chest x-ray and text dataset ########
class ChestXrayDataset(Dataset):
    """Chest X-ray dataset from https://nihcc.app.box.com/v/ChestXray-NIHCC."""

    def __init__(self, csv_file, transform=None):
        """
        Args:
            csv_file (string): Path to the csv file filename information.
            transform (callable, optional): Optional transform to be applied on a sample.
        """
        self.data_frame = pd.read_csv(csv_file)
        self.transform = transform
        self.tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')
        self.PRED_LABEL = [
            'Atelectasis',
            'Cardiomegaly', 
            'Consolidation',
            'Edema',
            'Enlarged Cardiomediastinum',
            'Fracture',
            'Lung Lesion',
            'Lung Opacity',
            'No Finding',
            'Pleural Effusion',
            'Pleural Other',
            'Pneumonia',
            'Pneumothorax',
            'Support Devices']

    def __len__(self):
        return len(self.data_frame)

    def __getitem__(self, idx):
        image_path = self.data_frame.iloc[idx]['dicom_id']
        image_path = image_path + ".jpg"
        image_path = ""+image_path
        image = Image.open(image_path)
        
        #### TODO: Read in image 
        image = np.asarray(image)
        image = torch.from_numpy(np.repeat(image[None,...],3,axis=0))        

        ###### TODO: return dictionary of image and corresponding label
        # torch.from_numpy(np.array(split_label.iloc[idx, 4:]).astype(int))
        label = torch.FloatTensor(np.zeros(len(self.PRED_LABEL), dtype=float))
        for i in range(0, len(self.PRED_LABEL)):
            if (self.data_frame[self.PRED_LABEL[i].strip()].iloc[idx].astype('float') > 0):
                label[i] = self.data_frame[self.PRED_LABEL[i].strip()].iloc[idx].astype('float')
                
        ################ text ###############
        text_path = self.data_frame.iloc[idx]['study_id']
        text_path = "s"+str(text_path)+".txt"
        text_path = ""+text_path
        
        with open(text_path, 'r') as f:
            text = f.read()
        clean_text = re.sub('[\\(\[#.!?,\'\/\])0-9]', ' ', str(text))
        clean_text = clean_text.replace('\n', ' ').replace('\r', '')
        clean_text = ' '.join(clean_text.split())
        encoding = self.tokenizer(
            clean_text,
            add_special_tokens=True,
            max_length=512,
            truncation=True,
            padding='max_length',
            return_attention_mask=True,
            return_tensors='pt'
        )
        
        if self.transform:
            image = self.transform(image)
        
        sample = {'image': image, 'label': label, 'input_ids':encoding['input_ids'].squeeze(0).clone().detach(), 'attention_mask':encoding['attention_mask'].squeeze(0).clone().detach()}
        
        return sample


    
###### Transform data and Dataloaders #######
train_transform = transforms.Compose([
        transforms.ToPILImage(),
        transforms.RandomResizedCrop(896),
        transforms.Resize(256),
        transforms.CenterCrop(224),
        transforms.RandomHorizontalFlip(),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])])


test_transform = transforms.Compose([
        transforms.ToPILImage(),
        transforms.CenterCrop(896),
        transforms.Resize(256),
        transforms.CenterCrop(224),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])])


train_loader = DataLoader(ChestXrayDataset(train_path, transform=train_transform), batch_size=BATCH_SIZE, shuffle=True)
valid_loader = DataLoader(ChestXrayDataset(valid_path, transform=train_transform), batch_size=BATCH_SIZE, shuffle=True) 
test_loader = DataLoader(ChestXrayDataset(test_path, transform=test_transform), batch_size=BATCH_SIZE, shuffle=True)



####### Multi-modal ######
def ResNet_model(pretrained):
    model = models.resnet50(progress=True, pretrained=pretrained)
    model.fc = nn.Linear(2048, 14)
    return model

class multi_modal(nn.Module):
    def __init__(self, num_classes):
        super(multi_modal, self).__init__()
        # resnet
        self.resnet = ResNet_model(pretrained=True)
        # bert
        self.bert = BertForSequenceClassification.from_pretrained('bert-base-uncased', num_labels=num_classes)
        # combine 
        self.classify = nn.Sequential(
            torch.nn.Linear(num_classes+num_classes, num_classes),
            torch.nn.Sigmoid()
        ) 
        
    def forward(self, input_ids, attention_mask, image):
        x1 = self.resnet(image)
        x2 = self.bert(input_ids=input_ids, attention_mask=attention_mask)
        x2 = x2['logits']
        embeds = torch.cat([x1, x2], dim=1)
        out = self.classify(embeds)
        return out
    


##### Train and evaluate functions #####
def train(model, iterator, optimizer, criterion):
    epoch_loss = 0
    epoch_acc = 0
    pred_label = []
    true_label = []

    model.train()
    
    for batch in iterator:
        optimizer.zero_grad()
        outputs = model(input_ids = batch['input_ids'].to(device), attention_mask = batch['attention_mask'].to(device), image = batch['image'].to(device))
        loss = criterion(outputs, batch['label'].type(torch.cuda.FloatTensor))
        loss.backward()
        optimizer.step()
        label = batch['label'].to(device)
        predicted_label = outputs > 0.5
        predicted_label = predicted_label.long().detach().cpu().numpy().tolist()
        actual_label = batch['label'].detach().cpu().numpy().tolist()
        epoch_loss += loss.item() * batch['image'].size(0)
        predicted = torch.round(outputs)
        epoch_acc += (predicted == label).sum().item()
        pred_label.extend(predicted_label)
        true_label.extend(actual_label)

    return epoch_loss / len(iterator.dataset),  epoch_acc / (len(iterator.dataset)*14), pred_label, true_label



def evaluate(model, iterator, criterion):
    epoch_loss = 0
    epoch_acc = 0
    pred_label = []
    true_label = []

    model.eval()

    with torch.no_grad():
        for batch in iterator:
            outputs = model(input_ids = batch['input_ids'].to(device), attention_mask = batch['attention_mask'].to(device), image = batch['image'].to(device))
            loss = criterion(outputs, batch['label'].type(torch.cuda.FloatTensor))
            label = batch['label'].to(device)
            predicted_label = outputs > 0.5
            predicted_label = predicted_label.long().detach().cpu().numpy().tolist()
            actual_label = batch['label'].detach().cpu().numpy().tolist()
            epoch_loss += loss.item() * batch['image'].size(0)
            predicted = torch.round(outputs)
            epoch_acc += (predicted == label).sum().item()

            pred_label.extend(predicted_label)
            true_label.extend(actual_label)

    return epoch_loss / len(iterator.dataset), epoch_acc / (len(iterator.dataset)*14), pred_label, true_label



##### Define parameters and  model outputs ######
model = multi_modal(14).to(device)
optimizer = optim.Adam(model.parameters(), lr=lr)
criterion = nn.BCELoss()

best_valid_loss = float('inf')
avg_loss_dict = {'train': [], 'valid': []}
avg_acc_dict = {'train': [], 'valid': []}
f1_score_dict = {'train': [], 'valid': []}
pred_dict = {'train': [], 'valid': []}
true_dict = {'train': [], 'valid': []}
auc_dict = {'train': [], 'valid': []}



##### Run #####
from sklearn.metrics import f1_score, roc_auc_score
from datetime import datetime

start = datetime.now()
print(f"Start time: {start}")


for epoch in range(epochs):
         
    train_loss, train_acc, train_pred, train_true = train(model, train_loader, optimizer, criterion)
    valid_loss, valid_acc, valid_pred, valid_true = evaluate(model, valid_loader, criterion)

    avg_loss_dict['train'].append(train_loss)
    avg_loss_dict['valid'].append(valid_loss)
 
    avg_acc_dict['train'].append(train_acc)
    avg_acc_dict['valid'].append(valid_acc)

    train_f1 = f1_score(train_true, train_pred, average=None)
    valid_f1 = f1_score(valid_true, valid_pred, average=None)
    
    f1_score_dict['train'].append(train_f1)
    f1_score_dict['valid'].append(valid_f1)

    train_auc = roc_auc_score(train_true, train_pred, average='macro')  
    valid_auc = roc_auc_score(valid_true, valid_pred, average='macro') 
    
    auc_dict['train'].append(train_auc)
    auc_dict['valid'].append(valid_auc)
    
    pred_dict['train'].append(train_pred)
    pred_dict['valid'].append(valid_pred)
    
    true_dict['train'].append(train_true)
    true_dict['valid'].append(valid_true)
    

    # save model with the lowest validation loss
    if valid_loss < best_valid_loss:
        best_valid_loss = valid_loss
        torch.save(model.state_dict(), MODEL_NAME)    

    print(f'| Epoch: {epoch+1:02} | Train Loss: {train_loss:.3f} | Train AUC: {train_auc*100:.2f}% | Train Acc: {train_acc*100:.2f}% | Val. Loss: {valid_loss:.3f} | Val. AUC: {valid_auc*100:.2f}% | Val. Acc: {valid_acc*100:.2f}% | ')


end = datetime.now()
print(f"End time: {end}")
print(f"Time taken: {end - start}")


##### Save model outputs ######
import pickle
with open(wd+MODEL_NAME+'.pkl', 'wb') as f:
    pickle.dump([avg_loss_dict, avg_acc_dict, f1_score_dict, pred_dict, true_dict, auc_dict, BATCH_SIZE, epochs, lr], f)


print("From resnet_bert_3.py")
