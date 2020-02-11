import java.io.*;

import java.io.BufferedReader; 
import java.io.File; 
import java.io.FileReader; 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ReadText1 {
   
    public static void main(String[] args){
       
       String[] a;
       ArrayList bus_x = new ArrayList();
       ArrayList bus_y = new ArrayList();
       String[] b;
       ArrayList user_x = new ArrayList();
       ArrayList user_y = new ArrayList();
       int start=0;
       int end=0;
       double value = 0;
       
       double[][] data;
       double bestsolution;
       
       JOptionPane.showMessageDialog(null,"ù������ ���������� �ι�°�� ���������� ��������");
         
         JFileChooser chooser1 = new JFileChooser(); //��ü ����
         JFileChooser chooser2 = new JFileChooser();
         
         chooser2.setMultiSelectionEnabled(true);

         int ret1 = chooser1.showOpenDialog(null);  //����â ����
         int ret2 = chooser2.showOpenDialog(null);
         
         File selectedFiles[] = chooser2.getSelectedFiles();
            

         if (ret1 != JFileChooser.APPROVE_OPTION) {

          JOptionPane.showMessageDialog(null, "������ ���������ʾҽ��ϴ�.",

            "���", JOptionPane.WARNING_MESSAGE);
          System.exit(0);
          

          return;

         }
         if (ret2 != JFileChooser.APPROVE_OPTION) {

             JOptionPane.showMessageDialog(null, "������ ���������ʾҽ��ϴ�.",

               "���", JOptionPane.WARNING_MESSAGE);
             System.exit(0);
             return;

            }

         String filePath1 = chooser1.getSelectedFile().getPath();  //���ϰ�θ� ������
         String filePath2 = chooser2.getSelectedFile().getPath();
      
         // ���� ���� �о����
         File busfile = new File(filePath1);   
         try {
            BufferedReader busFiles
             = new BufferedReader(new InputStreamReader(new FileInputStream(busfile.getAbsolutePath()), "UTF8"));
            
            String busline = "";
            while((busline = busFiles.readLine()) != null) {
                 if(busline.trim().length() > 0) {
                    int i = 0;
                     a = busline.split(",");
                     bus_x.add(a[1]);
                     bus_y.add(a[2]);
                     i++;
                 }
             }
            busFiles.close(); 
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         
         
         long totalstart_time = System.currentTimeMillis();
        // ���� ���� �о����
        for (int k=0, n=selectedFiles.length; k<n; k++) {
           File userfile = new File(selectedFiles[k].getPath());
           long start_time = System.currentTimeMillis();
          try {
               BufferedReader userFiles
               = new BufferedReader(new InputStreamReader(new FileInputStream(userfile.getAbsolutePath()), "UTF8"));
              
               String userline = "";
               
               while((userline = userFiles.readLine()) != null) {
                   if(userline.trim().length() > 0) {
                      int i = 0;
                       b = userline.split(",");  
                      user_x.add(b[1]);
                      user_y.add(b[2]);
                       i++;
                   }
               }
               userFiles.close();
           }
           
           catch (Exception e) {
               e.printStackTrace();
           }
           
           DTW dtw = new DTW(bus_x, bus_y, user_x, user_y);
           start = 0;
           end = user_x.size() - 1;
           bestsolution = dtw.INFINITE;
         
           while(end < bus_x.size()) {
              data = dtw.dpDistance(dtw.cutBus(start, end, bus_x), dtw.cutBus(start, end, bus_y), user_x, user_y);
              bestsolution = dtw.Backtrack(data, bestsolution, 0, 0);
              
              if(bestsolution < 2.5) {
                 dtw.result = true;
                 break;
              }
              else {
                 start = start + 10;
                 end = end + 10;
                 dtw.row_false=-1;
                 for(int l=0; l<user_x.size(); l++) {
                    for(int m=0; m<user_x.size(); m++)
                       dtw.dtw[l][m] = dtw.INFINITE;
                 }
                 dtw.result = false;
              }
           }
           user_x.clear();
           long end_time = System.currentTimeMillis();
           System.out.println(userfile+" : "+dtw.result+", bestSolution = "+bestsolution+" ���� �ð� : "+(end_time-start_time)/1000.0);
           
        }
        long totalend_time = System.currentTimeMillis();
        System.out.println("�� ���� �ð� : "+(totalend_time-totalstart_time)/1000.0);
    }
    
    public static class DTW {
       private static final double INFINITE = 1.0e+32;
       ArrayList user_x = new ArrayList(); // �����
       ArrayList user_y = new ArrayList();
       ArrayList bus_x = new ArrayList(); // ����
       ArrayList bus_y = new ArrayList();
       double[][] data;
       double[][] dtw;
       int row_false = -1;
       boolean result;
       
       public DTW(ArrayList bus_x, ArrayList bus_y, ArrayList user_x, ArrayList user_y) {
          this.user_x = user_x;
          this.user_y = user_y;
          this.bus_x = bus_x;
          this.bus_y = bus_y;
          this.dtw = new double[user_x.size()][user_x.size()];
       }
       
       public ArrayList cutBus(int start, int end, ArrayList bus) {
          ArrayList temp = new ArrayList();
           
            if(end < bus_x.size()) {
               for(int i=start; i <= end; i++)
                   temp.add(bus.get(i));
            }
            
            return temp;
        }
       
       private double[][] dpDistance(ArrayList bus_x, ArrayList bus_y, ArrayList user_x, ArrayList user_y) {
          double[][] data = new double[user_x.size()][user_x.size()];
          int d = user_x.size();
          
          for(int i=0; i<d; i++) {
             for(int j=0; j<d; j++) {
                data[i][j] = Math.sqrt(EuclideanDistance(Double.parseDouble((String) user_x.get(i)) , Double.parseDouble((String) bus_x.get(j))) + EuclideanDistance(Double.parseDouble((String) user_y.get(i)), Double.parseDouble( (String) bus_y.get(j))));
             }
          }
          return data;
       }
       
       public double EuclideanDistance(double x, double y) {

          double result = 0;
          result = (x - y) * (x - y);
          
          return result;
       }

     
       
        public double Backtrack(double[][] data, double bestSolution, int row, int col) { // data : dtw ������ ��Ʈ����, row : ��, col : ��
             
             if(row == user_x.size()) { // dtw�� ������ ���̸�
                if(dtw[row-1][row-1] < bestSolution) // �����ذ� bestSolution���� ������
                     bestSolution = dtw[row-1][row-1];
             }
             else {
                if(row==0 && col==0) { // dtw[0][0] �� �ʱ�ȭ
                   dtw[row][col] = data[0][0];
                   if (dtw[row][col] < bestSolution)
                      bestSolution = Backtrack(data, bestSolution, row,col+1);
                }
                else {
                   if(row==0){ // dtw ù��° �� �κ�
                      dtw[row][col] = data[0][col] + dtw[0][col-1];
                      if(dtw[row][col] < bestSolution) {
                           if(col < user_x.size()-1) 
                              bestSolution = Backtrack(data, bestSolution, 0,col+1);
                           else
                              bestSolution = Backtrack(data, bestSolution, row+1, 0);  
                        }
                      else // ���� ������
                         bestSolution = Backtrack(data, bestSolution, row+1, 0);
                   }
                   else if(col==0) { // dtw ù��° �� �κ�
                      if(row_false == -1) {
                         dtw[row][col] = data[row][0] + dtw[row-1][0];
                          if(dtw[row][col] < bestSolution) {
                             if(row < user_x.size()) // i �ε��� �˻�
                                bestSolution = Backtrack(data, bestSolution, row, col+1);
                          }
                            else {
                             row_false = row;
                             bestSolution = Backtrack(data, bestSolution, row, col+1);
                          }
                      }
                      else
                         bestSolution = Backtrack(data, bestSolution, row, col+1);
                     }
                         
                   else { // dtw[1][1] ����
                      if(dtw[row-1][col] > bestSolution && dtw[row][col-1] > bestSolution && dtw[row-1][col-1] > bestSolution) { // ��� bestSolution���� Ŭ��
                         if(col < user_x.size()-1)
                            bestSolution = Backtrack(data,bestSolution, row, col+1); // skip
                         else
                            bestSolution = Backtrack(data, bestSolution, row+1, 0);
                      }
                      else {
                         dtw[row][col] = data[row][col] + Math.min(dtw[row-1][col], Math.min(dtw[row][col-1],dtw[row-1][col-1] ));
                            if(col<user_x.size()-1)
                                bestSolution = Backtrack(data, bestSolution, row, col+1);
                            else
                                bestSolution = Backtrack(data, bestSolution, row+1, 0); 
                      }
                   }
                }
             }
             return bestSolution;
          }
       }
   }
