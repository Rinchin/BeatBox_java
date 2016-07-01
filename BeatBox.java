import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Rinchin on 30.06.16.
 */
public class BeatBox {
    JPanel mainPanel;

    //Here I store flags
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;

    //Intrument names - for labels in user interface
    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
            "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom",
            "High Agogo", "Open Hi Conga"};

    //Drum keys = like piano
    //35 = Bass Drum ...
    //...
    //63 = Open Hi Conga
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        BeatBox beatbox = new BeatBox();
        beatbox.buildGUI();
    }

    private void buildGUI() {
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);

        //Padding 10
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton serializeIt = new JButton("Serialize It");
        serializeIt.addActionListener(new MySendListener());
        buttonBox.add(serializeIt);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new MyReadInListener());
        buttonBox.add(restore);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);


        //Create empty flags with value false and then add them to ArrayList and to Panel
        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            if (i == 16) c.setSelected(true);
            else c.setSelected(false);
            checkboxList.add(c); //add checkbox to ArrayList
            mainPanel.add(c); // Add checkbox to Panel
        }

        setUpMidi();

        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    //Create MIDI code for synthesizer, sequencer and track
    private void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildTrackAndStart() {
        //Create array with 16 elements = intruments
        int[] trackList = null;

        //delete old track
        sequence.deleteTrack(track);
        //add new track
        track = sequence.createTrack();

        //do it for all 16 instruments = Bass, Congo ...
        for (int i = 0; i < 16; i++) {
            trackList = new int[16];

            //Key = instrument key
            int key = instruments[i];

            for (int j = 0; j < 16; j++) {
                JCheckBox jc = checkboxList.get(j + (16 * i));
                if (jc.isSelected()) //is tick checked then press key
                    trackList[j] = key;
                else
                    trackList[j] = 0; //nothing to do
            }

            makeTracks(trackList);//for this instrument and for all 16 ticks create events and add them to track
            track.add(makeEvent(176, 1, 127, 0, 16));
        }

        track.add(makeEvent(192, 9, 1, 0, 15)); //ckecking that event exists from 0 to 15 ticks
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY); //Infinite loop for melody,
            sequencer.start(); // melody start
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //method creates events for 1 instrument for all 16 ticks
    //if Get Array int[] for Instrument[Bass Drum] every element will be 0 or this instrument key
    private void makeTracks(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i)); // start play note
                //track.add(makeEvent(144,9,key,100,i+1)); // stop play note
            }
        }
    }

    private MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
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

    private class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();
        }
    }

    private class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    private class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoInBPM((float) (tempoFactor * 1.03));
        }
    }

    private class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoInBPM((float) (tempoFactor * .97));
        }
    }


    private class MySendListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            boolean[] checkboxState = new boolean[256];

            for (int i = 0; i < 256; i++) {
                JCheckBox check = checkboxList.get(i);
                if (check.isSelected()) {
                    checkboxState[i] = true;
                }
            }
            try {
                FileOutputStream fileStream = new FileOutputStream(new File("Checkbox.ser"));
                ObjectOutputStream os = new ObjectOutputStream(fileStream);
                os.writeObject(checkboxState);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class MyReadInListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            boolean[] checkboxState = new boolean[256];

            try {
                FileInputStream filein = new FileInputStream(new File("Checkbox.ser"));
                ObjectInputStream is = new ObjectInputStream(filein);
                checkboxState = (boolean[]) is.readObject();
            }
            catch(Exception e){
                e.printStackTrace();
            }


            for (int i = 0; i<256;i++){
                JCheckBox check = checkboxList.get(i);
                if (checkboxState[i])
                    check.setSelected(true);
                else
                    check.setSelected(false);
            }

            //stop playing
            sequencer.stop();

            buildTrackAndStart();
        }
    }
}