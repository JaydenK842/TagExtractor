import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static java.nio.file.StandardOpenOption.CREATE;

public class FileReaderRunner extends JFrame {
    Vector<String> stopWords = new Vector<>();
    Vector<String> allWords = new Vector<>();
    Map<String, Integer> tags = new HashMap<>();

    JFrame frame;
    JPanel namePanel;
    JPanel textPanel;
    JPanel buttonPanel;
    JTextField title;
    JTextArea tagFrequency;
    JScrollPane scroll;
    JButton choice;
    JButton save;

    public FileReaderRunner() {
        Toolkit kit = Toolkit.getDefaultToolkit();
        frame = new JFrame();
        frame.setLayout(null);

        //Finds the users screen height and width
        Dimension screenSize = kit.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;

        //Centers the frame in the middle of the user's screen
        frame.setSize(screenWidth / 2, screenHeight / 2);
        frame.setLocation(screenWidth / 4, screenHeight / 4);

        //Runs design()
        design();
        //Sets the location of the title
        namePanel.setBounds((frame.getWidth() / 3) - 75, 10, 400, 50);
        frame.add(namePanel);
        //Sets the location of the text area
        textPanel.setBounds((frame.getWidth() / 3) - 75, 70, 400, 215);
        frame.add(textPanel);
        //Sets the location of the buttons
        buttonPanel.setBounds((frame.getWidth() / 3) - 75, 300, 400, 50);
        frame.add(buttonPanel);

        try {
            //Opens the stop words file
            File selectedFile = new File("src/EnglishUpdated.txt");
            Path file = selectedFile.toPath();

            //Weird code used for reading files
            InputStream in =
                    new BufferedInputStream(Files.newInputStream(file, CREATE));
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(in));

            //Reads a line of the file and adds it to a vector
            while (reader.ready()) {
                String line = reader.readLine();
                stopWords.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Sets the GUI to have a default close and makes it visible
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void design() {
        //Creates a title, formats it and adds it to the namePanel
        namePanel = new JPanel();
        namePanel.setBorder(new LineBorder(Color.BLACK, 1));
        title = new JTextField("File Not Chosen");
        title.setEditable(false);
        title.setBorder(null);
        title.setFont(new Font("Arial", Font.PLAIN, 25));
        title.setPreferredSize(new Dimension(395, 35));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        namePanel.add(title);

        //Creates a textArea, puts a vertical scroll bar on it, then adds it to the textPanel
        textPanel = new JPanel();
        tagFrequency = new JTextArea(13, 37);
        tagFrequency.setEditable(false);
        scroll = new JScrollPane(tagFrequency);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        textPanel.add(scroll);

        //Creates two buttons
        //One that lets you choose the file
        //Another that saves your results
        //Then adds it to the buttonPanel
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 0));
        choice = new JButton("Choose File");
        choice.setFocusable(false);
        choice.addActionListener((ActionEvent ae) -> openFile(title, tagFrequency));
        buttonPanel.add(choice);
        save = new JButton("Save To File");
        save.setFocusable(false);
        save.addActionListener((ActionEvent ae) -> fileSave(tagFrequency));
        buttonPanel.add(save);
    }

    private void openFile(JTextField title, JTextArea tagFrequency) {
        JFileChooser chooser = new JFileChooser();
        File selectedFile;

        try {
            //Creates and sets a directory
            File workingDirectory= new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);

            //If they selected a file, it will run the code
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                //Gets the file path
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();

                //Changes the tile to the file name
                title.setText(String.valueOf(file.getFileName()));

                //Weird code for reading files
                InputStream in =
                        new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in));

                //While there is still a line to read
                while (reader.ready()) {
                    boolean isAStopWord = false;
                    String rec = reader.readLine();
                    String[] list;
                    list = rec.split(" ");

                    //For each word per line:
                    for (String item : list) {
                        //Makes the word all lowercase
                        item = item.toLowerCase();

                        //If it already has been found, it's counter gains one
                        if (tags.containsKey(item)) {
                            tags.put(item, tags.get(item) + 1);
                        } else {
                            //Checks if the word is a stop word
                            for (String word : stopWords) {
                                if (item.equals(word)) {
                                    isAStopWord = true;
                                    break;
                                }
                            }

                            //If it wasn't a stop word, it is added to the map and allWords
                            if (!isAStopWord) {
                                tags.put(item, 1);
                                allWords.add(item);
                            }
                        }
                    }
                }

                //For each key in allWords:
                for (String key : allWords) {
                    //Probably shouldn't have the < 13
                    //The >= 3211 is to get rid of it counting empty lines
                    if (tags.get(key) < 13 || tags.get(key) >= 3211) {
                        tags.remove(key);
                    } else if (key.contains("\"") || key.contains(".") || key.contains(",") || key.contains("“") || key.contains("]")) {
                        //Too vague, but if it contains one special character above
                        //It is completely removed
                        tags.remove(key);
                    } else {
                        //Otherwise, the word and how many times it was found it output in the textArea
                        tagFrequency.append(key + ": " + tags.get(key) + "\n");
                    }
                }
            } else {
                //Lets the user know they failed to choose a file
                JOptionPane.showMessageDialog(frame, "Failed to choose a file.");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fileSave(JTextArea tagFrequency) {
        //Asks the user what they want to the name of the file to be
        String newFileName = JOptionPane.showInputDialog(frame, "Name the File:");

        //If they hit cancel, it will not save the file
        if (newFileName == null) {
            return;
        } else if (newFileName.equals("") || newFileName.contains(" ")) {
            //If they give it a blank name or put a space in it,
            //it will tell them it isn't a potential name
            JOptionPane.showMessageDialog(frame, "That is not a viable name.");
            return;
        }

        //Creates the path for the file
        File workingDirectory = new File(System.getProperty("user.dir"));
        Path file = Paths.get(workingDirectory.getPath() + "\\src\\" + newFileName + ".txt");

        try {
            //Weird code for writing to a file
            OutputStream out =
                    new BufferedOutputStream(Files.newOutputStream(file, CREATE));
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(out));

            //Puts everything in the textArea on their file
            writer.write(tagFrequency.getText());

            //Closes the writer and lets the user know their file was saved
            writer.close();
            JOptionPane.showMessageDialog(frame, "Your file has been saved!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
