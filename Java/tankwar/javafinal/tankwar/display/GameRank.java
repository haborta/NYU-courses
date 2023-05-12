package javafinal.tankwar.display;

import javafinal.tankwar.Battlefield;
import javafinal.tankwar.Client;
import javafinal.tankwar.Predefined;

import java.awt.*;

public class GameRank {
    private int w;
    private int h;

    private Client user;

    private int width = 400;
    private int height = 22;

    private String[] allScores;

    private boolean hasRank;

    private Image image = Predefined.loadImageIcon("tank_bg.png").getImage();

    public GameRank(Client user, boolean hasRank) {
        this.w = Battlefield.windowWidth;
        this.h = Battlefield.windowHeight;

        this.user = user;
        this.allScores = user.allScores.split(",");
        this.hasRank = hasRank;
       // System.out.println(allScores.length);
    }

    public void draw(Graphics g) {

        g.drawImage(image, 0, 0, null);

        if (hasRank) {

            String str1 = "Only Top 10 score records are here!";
            Color c = new Color(250, 140, 40);
            Font f1 = new Font("Arial", Font.BOLD, 27);
            Font f2 = new Font("Arial", Font.BOLD, 17);
            g.setColor(c);
            g.setFont(f1);
            int x1 = (w - g.getFontMetrics(f1).stringWidth(str1)) / 2;
            int y1 = 50;
            g.drawString(str1, x1, y1);
            String str2 = "Less than 10 if we just have few players";
            g.setColor(Color.BLACK);
            g.setFont(f2);
            int x2 = (w - g.getFontMetrics(f2).stringWidth(str2)) / 2;
            int y2 = 100;
            g.drawString(str2, x2, y2);

            int x = (w - width) / 2;
            int y = 150;
            int interval = 5;

            for (String score: allScores) {
                String[] info = score.split(" == ");
                String str = info[0] + " got " + info[1] + " score(s) at " + info[2];
                g.setColor(new Color(230, 180, 0));
                g.fill3DRect(x, y, width, height, true);
                g.setColor(Color.BLACK);
                Font f = new Font("Arial", Font.PLAIN, 15);
                g.setFont(f);
                g.drawString(str, x + (width - g.getFontMetrics(f).stringWidth(str)) / 2 , y + 15);
                y += height;
                y += interval;
            }

            String str3 = "Please press \"ESCAPE\" to return to the main menu!";

            Font f = new Font("Arial", Font.BOLD, 15);
            g.setColor(Color.gray);
            int rectWidth = g.getFontMetrics(f).stringWidth(str3) + 20;
            int rectHeight = 25;
            g.fill3DRect((w - rectWidth) / 2, 450, rectWidth, rectHeight, true);

            g.setColor(Color.BLACK);
            g.setFont(f);
            int x3 = (w - g.getFontMetrics(f).stringWidth(str3)) / 2;
            int y3 = 470;		//center - up space
            g.drawString(str3, x3, y3);

        } else {
            String str = "Sorry, there is no rank right now!";

            Font f = new Font("Arial", Font.BOLD, 30);
            g.setColor(Color.BLACK);
            g.setFont(f);
            g.drawString(str, (w - g.getFontMetrics(f).stringWidth(str)) / 2, h / 2);

            str = "Please press \"ESCAPE\" to return!";

            f = new Font("Arial", Font.BOLD, 15);

            g.setColor(Color.gray);
            int rectWidth = g.getFontMetrics(f).stringWidth(str) + 20;
            int rectHeight = 25;
            g.fill3DRect((w - rectWidth) / 2, h / 2 + 80, rectWidth, rectHeight, true);


            g.setColor(Color.BLACK);
            g.setFont(f);
            int x2 = (w - g.getFontMetrics(f).stringWidth(str)) / 2;
            int y2 = h / 2 + 100;		//center - up space
            g.drawString(str, x2, y2);

        }

    }
}
