########## LIBRARY #########
import pandas as pd
import torch
import numpy as np
from PIL import Image
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
from torchvision.transforms import Compose, Resize, CenterCrop
from transformers import CLIPProcessor, CLIPModel, CLIPConfig


######### DEVICE ###########
if torch.cuda.is_available():      
    device = torch.device("cuda")
    print('There are %d GPU(s) available.' % torch.cuda.device_count())
    print('We will use the GPU:', torch.cuda.get_device_name(0))
else:
    print('No GPU available, using the CPU instead.')
    device = torch.device("cpu")

print("Device being used: %s" %device)


####### RANDOM SEED ########
import torch
import numpy as np
import random

random_seed = 2023
torch.manual_seed(random_seed)
random.seed(random_seed)
np.random.seed(random_seed)


######## LOAD DATA #########
import pandas as pd
import os

wd = ""
os.chdir(wd)
print(f"Working dir: {wd}")

train_path = wd+"train_subset_3.csv"
valid_path = wd+"valid_subset_3.csv"
test_path = wd+"test_subset_3.csv"

train_subset = pd.read_csv(train_path)
valid_subset = pd.read_csv(valid_path)
test_subset = pd.read_csv(test_path)
print(f"Train size: {len(train_subset)}")
print(f"Valid size: {len(valid_subset)}")

# train_subset = pd.read_csv("train_subset_2.csv")
# valid_subset = pd.read_csv("valid_subset_2.csv")
# test_subset = pd.read_csv("test_subset_2.csv")

# train_subset = pd.read_csv("train_subset_3.csv")
# valid_subset = pd.read_csv("valid_subset_3.csv")
# test_subset = pd.read_csv("test_subset_3.csv")


######## HYPERPARAMETERS #########
BATCH_SIZE = 64
lr = 0.001
NUM_EPOCHS = 24
MODEL_NAME = "clip_1.pt"

print(f"Batch size: {BATCH_SIZE}")
print(f"Learning rate: {lr}")
print(f"Number of epochs: {NUM_EPOCHS}")
print(f"Model name: {MODEL_NAME}")


######### DATASET CLASS ########
import re
import string
import torch.nn as nn
from sklearn.metrics import f1_score, roc_auc_score


class MIMICDataset(Dataset):
    def __init__(self, csv_path):
        self.df = pd.read_csv(csv_path)
        self.image_transform = Compose([
            transforms.Resize((224,224)), 
            transforms.CenterCrop((224,224))
        ])
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
        self.processor = CLIPProcessor.from_pretrained('openai/clip-vit-base-patch32')
        
    def __len__(self):
        return len(self.df)
    def __getitem__(self, idx):
        # Load image
        image_path = self.df.iloc[idx]['dicom_id']
        image_path = image_path + ".jpg"
        image_path = ""+image_path
        image = Image.open(image_path)
        # image = io.imread(image_path)
        # image = color.rgb2gray(image)
        # image = image/image.max()
        image = np.asarray(image)
        image = torch.from_numpy(np.repeat(image[None,...],3,axis=0))
        # the resize and normalize operation will be processed inner CLIPProcessor
        
        # Load text
        text_path = self.df.iloc[idx]['study_id']
        text_path = "s"+str(text_path)+".txt"
        text_path = ""+text_path

        with open(text_path, 'r') as f:
            text = f.read()
        # clean special character, \n and extra space
        # could use nltk to modify the clean_text
        # in clip, the maximum sequence length for this model is 77
        clean_text = ''.join(char for char in text if char not in string.punctuation)
        clean_text = clean_text.replace('\n', ' ').replace('\r', '')
        clean_text = ' '.join(clean_text.split())

        # Load label
        label = torch.FloatTensor(np.zeros(len(self.PRED_LABEL), dtype=float))
        for i in range(0, len(self.PRED_LABEL)):
            if (self.df[self.PRED_LABEL[i].strip()].iloc[idx].astype('float') > 0):
                label[i] = self.df[self.PRED_LABEL[i].strip()].iloc[idx].astype('float')
        
        #Prepare inputs for CLIP model
        inputs = self.processor(text = clean_text, images = image, return_tensors="pt", padding="max_length", max_length=77, truncation=True)
        return (inputs, label)


####### LOAD CSV FILE AND DATALOADER ########
train_loader = DataLoader(MIMICDataset(train_path), batch_size=BATCH_SIZE, shuffle=True)
valid_loader = DataLoader(MIMICDataset(valid_path), batch_size=BATCH_SIZE, shuffle=True) 
test_loader = DataLoader(MIMICDataset(test_path), batch_size=BATCH_SIZE, shuffle=True) 


####### CLIP MODEL ########
class clip_class(nn.Module):
    def __init__(self, num_classes):
        super(clip_class, self).__init__()
        self.clip = CLIPModel.from_pretrained("openai/clip-vit-base-patch32")
        self.classify = nn.Sequential(
            torch.nn.Linear(512+512, 256),
            torch.nn.ReLU(),
            torch.nn.Linear(256, num_classes),
            torch.nn.Sigmoid()
        )    # Add more linear layer will cause a poor result, don't know why
        
    def forward(self, input_ids, attention_mask, pixel_values):
        output = self.clip(input_ids = input_ids, attention_mask = attention_mask, pixel_values = pixel_values)
        embeds = torch.cat([output['text_embeds'], output['image_embeds']], dim=1)
#         embeds = torch.cat([output['text_model_output']['pooler_output'], output['vision_model_output']['pooler_output']], dim=1) #512+768
        out = self.classify(embeds)
        return out


######## TRAIN AND EVALUATE ######
def train(model, optimizer, criterion, train_loader, valid_loader, num_epochs=5):
    train_loss_list, train_acc_list, valid_loss_list, valid_acc_list = [], [], [], []
    train_true_label, train_pred_label, valid_true_label, valid_pred_label = [], [], [], []
    best_valid_loss = float('inf')
    
    for epoch in range(num_epochs):
        train_loss, train_acc = 0, 0
        model.train()
        for inputs, labels in train_loader:
            input_ids, attention_mask,  pixel_values = torch.squeeze(inputs['input_ids'],1).to(device), torch.squeeze(inputs['attention_mask'],1).to(device), torch.squeeze(inputs['pixel_values'], 1).to(device)
            labels = labels.to(device)
            optimizer.zero_grad()
            output= model(input_ids = input_ids, attention_mask = attention_mask, pixel_values = pixel_values)
            predicted = torch.round(output)
            loss = criterion(output, labels)
            loss.backward()
            optimizer.step()
            
            train_loss += loss.item() * input_ids.size(0)
            train_acc += (predicted == labels).sum().item()
            
            train_true_label.extend(labels.detach().cpu().numpy().tolist())
            train_pred_label.extend(predicted.detach().cpu().numpy().tolist())

        train_loss /= len(train_loader.dataset)
        train_acc /= (len(train_loader.dataset)*14)
        train_loss_list.append(train_loss)
        train_acc_list.append(train_acc)
        
        #### valid ####
        val_loss, val_acc = 0, 0
        model.eval()
        for inputs, labels in valid_loader:
            input_ids, attention_mask,  pixel_values = torch.squeeze(inputs['input_ids'],1).to(device), torch.squeeze(inputs['attention_mask'],1).to(device), torch.squeeze(inputs['pixel_values'], 1).to(device)
            labels = labels.to(device)
            output= model(input_ids = input_ids, attention_mask = attention_mask, pixel_values = pixel_values)
            predicted = torch.round(output)
            loss = criterion(output, labels)
            
            val_loss += loss.item() * input_ids.size(0)
            val_acc += (predicted == labels).sum().item()
            
            valid_true_label.extend(labels.detach().cpu().numpy().tolist())
            valid_pred_label.extend(predicted.detach().cpu().numpy().tolist())
      
        val_loss /= len(valid_loader.dataset)
        
        if val_loss < best_valid_loss:
            best_valid_loss = val_loss
            torch.save(model.state_dict(), MODEL_NAME)

        val_acc /= (len(valid_loader.dataset)*14)
        valid_loss_list.append(val_loss)
        valid_acc_list.append(val_acc)    
        print('Epoch: {} \tTraining Loss: {:.6f} \tValidation Loss: {:.6f} \tTrain Accuracy: {:.6f} \tValidation Accuracy: {:.6f} '.format(
            epoch, train_loss, val_loss, train_acc, val_acc))
#         torch.save(model.state_dict(), "./clip_test_result/model/" + 'clip_classify{}.pt'.format(epoch + 1))
    return train_loss_list, train_acc_list, train_true_label, train_pred_label, valid_loss_list, valid_acc_list, valid_true_label, valid_pred_label


######## TEST ##########
def test(model, criterion, test_loader):
    test_loss, test_acc = 0.0, 0.0
    y_output = []
    y_predicted = []
    y_true = []
    model.eval()
    for inputs, labels in test_loader:
        input_ids, attention_mask,  pixel_values = torch.squeeze(inputs['input_ids'],1).to(device), torch.squeeze(inputs['attention_mask'],1).to(device), torch.squeeze(inputs['pixel_values'], 1).to(device)
        labels = labels.to(device)
        output= model(input_ids = input_ids, attention_mask = attention_mask, pixel_values = pixel_values)
        predicted = torch.round(output)

        y_output.append(output.tolist())
        y_predicted.append(predicted.tolist())
        y_true.append(labels.tolist())
        
        test_acc += (predicted == labels).sum().item()
        loss = criterion(output, labels)
        test_loss += loss.item() * input_ids.size(0)
        
    test_acc /= (len(test_loader.dataset)*14)
    test_loss /= len(test_loader.dataset)
    return test_loss, test_acc, y_output, y_predicted, y_true



############## RUN ################
from datetime import datetime

start = datetime.now()
print(f"Start time: {start}")

model = clip_class(14)
model = model.to(device)
criterion = torch.nn.BCELoss()
optimizer = torch.optim.Adam(model.parameters(), lr=lr)

# change num_epochs, 1 for test
train_loss_list, train_acc_list, train_true_label, train_pred_label, valid_loss_list, valid_acc_list, valid_true_label, valid_pred_label = train(model, optimizer, criterion, train_loader, valid_loader, num_epochs=NUM_EPOCHS)

end = datetime.now()
print(f"End time: {end}")
print(f"Time taken: {end - start}")



####### SAVE OUTPUTS #######
import pickle
with open(wd+MODEL_NAME+'.pkl', 'wb') as f:
    pickle.dump([train_loss_list, train_acc_list, train_true_label, train_pred_label, valid_loss_list, valid_acc_list, valid_true_label, valid_pred_label, BATCH_SIZE, NUM_EPOCHS, lr], f)


print("From clip_train_1.py")
