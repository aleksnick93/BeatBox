package com.test;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class BeatBox {

    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxList;
    Sequencer sqr;
    Sequence seq;
    Track trk;
    JFrame frame;

    String[] instrumentNames = {"Bass Drum", "Closet Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
        "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
        "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
	    // write your code here
        new BeatBox().buildGUI();
    }

    public void buildGUI() {
        frame = new JFrame("Cyber beatBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel backgroung = new JPanel(layout);
        backgroung.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkBoxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int i = 0; i < 16; i++){
            nameBox.add(new Label(instrumentNames[i]));
        }

        backgroung.add(BorderLayout.EAST, buttonBox);//Another code for GUI
        backgroung.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(backgroung);

        //GridLayout grid = new GridLayout(16, 32);
        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New composition");
        JMenuItem loadMenuItem = new JMenuItem("Load composition");
        JMenuItem saveMenuItem = new JMenuItem("Save composition");
        newMenuItem.addActionListener(new MyNewListener());
        loadMenuItem.addActionListener(new MyReadInListener());
        saveMenuItem.addActionListener(new MySendListener());
        fileMenu.add(newMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);

        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        mainPanel = new JPanel(grid);
        backgroung.add(BorderLayout.CENTER,mainPanel);

        //for (int i = 0; i < 512; i++) { //Create a flags, initialization them false, then add it to the ArrayList
        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox(); // and on panel
            c.setSelected(false);
            checkBoxList.add(c);
            mainPanel.add(c);
        }

        setUpMidi();

        frame.setBounds(50, 50, 300, 500);
        frame.pack();
        frame.setVisible(true);
    }

    public void setUpMidi() {
        try {
            sqr = MidiSystem.getSequencer();
            sqr.open();
            seq = new Sequence(Sequence.PPQ,4);
            trk = seq.createTrack();
            sqr.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildTrackAndStart() {
        int[] trackList = null;

        seq.deleteTrack(trk);
        trk = seq.createTrack();

        for(int i = 0; i < 16; i++){
            //trackList = new int[32];
            trackList = new int[16];

            int key = instruments[i];

            //for (int j = 0; j < 32; j++) {
            for (int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) checkBoxList.get(j + (16 * i));
                if (jc.isSelected())
                    trackList[j] = key;
                else
                    trackList[j] = 0;
            }

            makeTracks(trackList);
            trk.add(makeEvent(176, 1, 127, 0, 16));
        }

        trk.add(makeEvent(192, 9, 1, 0, 15));
        try {
            sqr.setSequence(seq);
            sqr.setLoopCount(sqr.LOOP_CONTINUOUSLY);
            sqr.start();
            sqr.setTempoInBPM(120);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public class MySendListener implements ActionListener {
        public void actionPerformed(ActionEvent event){
            boolean[] checkboxState = new boolean[256];//There 512
            JFileChooser fileSave = new JFileChooser();
            fileSave.showSaveDialog(frame);

            for(int i = 0; i < 256; i++){//There 512
                JCheckBox check = (JCheckBox) checkBoxList.get(i);
                if(check.isSelected()) {
                    checkboxState[i] = true;
                }
            }

            try{
                FileOutputStream fileStream = new FileOutputStream(fileSave.getSelectedFile());
                ObjectOutputStream os = new ObjectOutputStream(fileStream);
                os.writeObject(checkboxState);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class MyReadInListener implements ActionListener {
        public void actionPerformed(ActionEvent event){
            boolean[] checkboxState = null;
            JFileChooser fileOpen = new JFileChooser();
            fileOpen.showOpenDialog(frame);
            try {
                FileInputStream fileIn = new FileInputStream(fileOpen.getSelectedFile());
                ObjectInputStream is = new ObjectInputStream(fileIn);
                checkboxState = (boolean[]) is.readObject();
            } catch (Exception ex){
                ex.printStackTrace();
            }

            for (int i = 0; i < 256; i++){//There 512
                JCheckBox check = (JCheckBox) checkBoxList.get(i);
                if(checkboxState[i]){
                    check.setSelected(true);
                } else {
                    check.setSelected(false);
                }

                sqr.stop();
                buildTrackAndStart();
            }
        }
    }

    public class MyNewListener implements ActionListener {
        public void actionPerformed(ActionEvent event){
            boolean[] checkboxState = new boolean[256];//There 512

            for(int i = 0; i < 256; i++){//There 512
                JCheckBox check = (JCheckBox) checkBoxList.get(i);
                check.setSelected(false);
            }

            sqr.stop();
        }
    }

    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            sqr.stop();
        }
    }

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sqr.getTempoFactor();
            sqr.setTempoFactor((float) (tempoFactor * 1.05));
        }
    }

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sqr.getTempoFactor();
            sqr.setTempoFactor((float) (tempoFactor * .95));
        }
    }

    public void makeTracks(int[] list) {
        for(int i = 0; i < 16; i++){//There 32
            int key = list[i];

            if(key != 0) {
                trk.add(makeEvent(144, 9, key, 100, i));
                trk.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }
}
