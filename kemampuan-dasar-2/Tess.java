package tess;


import com.fazecast.jSerialComm.SerialPort;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tess {
    static SerialPort chosenPort;
    static int x;
    public static void main(String[] args) {
        JFrame frame1 = new JFrame();
        frame1.setBounds(100,100,1200,700);
        frame1.setTitle("latihan buat frame");
        frame1.setSize(600, 400);
        frame1.setLocation(0, 0);
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComboBox<String> portList = new JComboBox<>();
        JButton connectButton = new JButton("Connect");
        JPanel topPanel = new JPanel();
        topPanel.add(portList);
        topPanel.add(connectButton);
        frame1.add(topPanel, BorderLayout.NORTH);

        JTextField textEmosi =  new JTextField();
        textEmosi.setColumns(10);
        JLabel NamaEmosi = new JLabel("Emotion Name");
        JLabel PicEmosi = new JLabel("");
        JPanel topPanel2 = new JPanel();
        //JPanel topPanel3 = new JPanel();
        topPanel2.add(NamaEmosi);
        topPanel2.add(textEmosi);
        topPanel2.add(PicEmosi);
        frame1.add(topPanel2, BorderLayout.SOUTH);


        SerialPort[] portNames = SerialPort.getCommPorts();
        for (SerialPort portName : portNames) {
            portList.addItem(portName.getSystemPortName());
        }
        //membuat grafik
        XYSeries series = new XYSeries("ECG Sensor Signal");
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(" ECG sensor Display","sample","ADC",dataset);
        XYPlot xyplot = (XYPlot) chart.getPlot();
        XYItemRenderer renderer = xyplot.getRenderer();
        renderer.setSeriesPaint(0, Color.red);
        NumberAxis domain1 = (NumberAxis) xyplot.getDomainAxis();
        domain1.setRange(0,2000);
        ChartPanel panel1 = new ChartPanel(chart);
        panel1.setBounds(28,80,1116, 252);
        frame1.getContentPane().add(panel1);

        XYSeries series1 = new XYSeries("EEG Sensor Signal");
        XYSeriesCollection dataset1 = new XYSeriesCollection(series1);
        JFreeChart chart1 = ChartFactory.createXYLineChart("EEG Sensor Display","sample","ADC",dataset1);
        XYPlot xyplot1 = (XYPlot) chart1.getPlot();
        XYItemRenderer renderer1 = xyplot1.getRenderer();
        renderer1.setSeriesPaint(0, Color.blue);
        NumberAxis domain2 = (NumberAxis) xyplot1.getDomainAxis();
        domain2.setRange(0,2000);
        ChartPanel panel2 = new ChartPanel(chart1);
        panel2.setBounds(28,80,1116, 252);
        frame1.getContentPane().add(panel2);

        //membuatsplit
        JSplitPane splitGrafik = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,panel1,panel2);
        //splitGrafik.setDividerLocation(275);
        frame1.add(splitGrafik);

        JButton deteksiButton = new JButton("Detection");
        JButton saveButton = new JButton("Save");
        //JButton saveButton = new JButton("Save");
        //JPanel bottomPanel = new JPanel();
        topPanel.add(deteksiButton);
        topPanel.add(saveButton);

        saveButton.addActionListener((ActionEvent arg0) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", "txt");
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setDialogTitle("Specify a file to save");
            
            int userSelection = fileChooser.showSaveDialog(frame1);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave=fileChooser.getSelectedFile();
                Path to= Paths.get(fileToSave.getAbsoluteFile()+".txt");
                Path from=Paths.get("D:/temp.csv");
                try {
                    Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    Logger.getLogger(Tess.class.getName()).log(Level.SEVERE, null, ex);
                }

                
            }
        });
        deteksiButton.addActionListener((ActionEvent arg0) -> {
            try {
                ///////////////////////Calculate Emotion Start
                File file = new File("D:/temp.csv");
                double sum;
                double numberofData;
                try (Scanner inputStream = new Scanner(file)) {
                    sum = 0;
                    numberofData = 0;
                    while (inputStream.hasNext()){
                        String data = inputStream.next();
                        String[] values = data.split(",");
                        double avData = Double.parseDouble(values [1]);
                        if(avData>0){
                            sum += avData; numberofData++;
                        }
                        //System.out.println(avData);
                    }
                }
                //System.out.println("Average - " + (sum / numberofData));
                ///////////////////////Calculate Emotion End
                double rerata=(sum / numberofData);
                if(rerata>=0&&rerata<=3){
                    textEmosi.setText("POSITIVE");
                    PicEmosi.setIcon(new ImageIcon(ImageIO.read(new File(System.getProperty("user.dir")+"/src/bpm/emoticon/happy.png"))));
                }
                else if(rerata>=3.01){
                    textEmosi.setText("NEGATIVE");
                    PicEmosi.setIcon(new ImageIcon(ImageIO.read(new File(System.getProperty("user.dir")+"/src/bpm/emoticon/sad.png"))));
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Tess.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Tess.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        // configure the connect button and use another thread to listen for data
        connectButton.addActionListener((ActionEvent arg0) -> {
            if(connectButton.getText().equals("Connect")) {
                // attempt to connect to the serial port
                chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
                chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
                
                if(chosenPort.openPort()) {
                    connectButton.setText("Disconnect");
                    portList.setEnabled(false);
                }
// create a new thread that listens for incoming text and populates the graph
Thread thread;
                thread = new Thread(){
                    @Override public void run() {
                        FileWriter fw = null;
                        try {
                            
                            BufferedWriter bw;
                            try (Scanner scanner = new Scanner(chosenPort.getInputStream())) {
                                File file = new File("d:/temp.csv");
                                fw = new FileWriter(file);
                                bw = new BufferedWriter(fw);
                                while(scanner.hasNextLine()) {
                                    try {
                                        String line = scanner.nextLine();
                                        System.out.println(line);
                                        int number = Integer.parseInt(line);
                                        if (x > 2000) {
                                            domain1.setRange(x-2000,x);
                                            domain2.setRange(x-2000,x);
                                            series.add(x++, number);
                                            series1.add(x++, number);
                                            frame1.repaint();
                                            bw.write(x + "," + number);
                                            bw.newLine();
                                        }
                                        else {
                                            series.add(x++, number);
                                            series1.add(x++, number);
                                            frame1.repaint();
                                            bw.write(x + "," + number);
                                            bw.newLine();
                                        }
                                    } catch(IOException | NumberFormatException e) {}
                                }
                            }
                            bw.close();
                            fw.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Tess.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            try {
                                fw.close();
                            } catch (IOException ex) {
                                Logger.getLogger(Tess.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                };
thread.start();
            } else {
                // disconnect from the serial port
                chosenPort.closePort();
                portList.setEnabled(true);
                connectButton.setText("Connect");
                
                
            }
        });
        frame1.setVisible(true);
    }
}
