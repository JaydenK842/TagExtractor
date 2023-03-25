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

        Dimension screenSize = kit.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;

        //Centers the frame in the middle of the user's screen
        frame.setSize(screenWidth / 2, screenHeight / 2);
        frame.setLocation(screenWidth / 4, screenHeight / 4);

        design();
        namePanel.setBounds((frame.getWidth() / 3) - 75, 10, 400, 50);
        frame.add(namePanel);
        textPanel.setBounds((frame.getWidth() / 3) - 75, 70, 400, 215);
        frame.add(textPanel);
        buttonPanel.setBounds((frame.getWidth() / 3) - 75, 300, 400, 50);
        frame.add(buttonPanel);

        try {
            File selectedFile = new File("src/EnglishUpdated.txt");
            Path file = selectedFile.toPath();

            InputStream in =
                    new BufferedInputStream(Files.newInputStream(file, CREATE));
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(in));

            while (reader.ready()) {
                String line = reader.readLine();
                stopWords.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void design() {
        namePanel = new JPanel();
        namePanel.setBorder(new LineBorder(Color.BLACK, 1));
        title = new JTextField("File Not Chosen");
        title.setEditable(false);
        title.setBorder(null);
        title.setFont(new Font("Arial", Font.PLAIN, 25));
        title.setPreferredSize(new Dimension(395, 35));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        namePanel.add(title);

        textPanel = new JPanel();
        tagFrequency = new JTextArea(13, 37);
        tagFrequency.setEditable(false);
        scroll = new JScrollPane(tagFrequency);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        textPanel.add(scroll);

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
            File workingDirectory= new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();

                title.setText(String.valueOf(file.getFileName()));

                InputStream in =
                        new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in));

                while (reader.ready()) {
                    boolean isAStopWord = false;
                    String rec = reader.readLine();
                    String[] list;
                    list = rec.split(" ");

                    for (String item : list) {
                        item = item.toLowerCase();
                        if (tags.containsKey(item)) {
                            tags.put(item, tags.get(item) + 1);
                        } else {
                            for (String word : stopWords) {
                                if (item.equals(word)) {
                                    isAStopWord = true;
                                    break;
                                }
                            }

                            if (!isAStopWord) {
                                tags.put(item, 1);
                                allWords.add(item);
                            }
                        }
                    }
                }

                for (String key : allWords) {
                    if (tags.get(key) < 13 || tags.get(key) == 3211) {
                        tags.remove(key);
                    } else if (key.contains("\"") || key.contains(".") || key.contains(",") || key.contains("“") || key.contains("]")) {
                        tags.remove(key);
                    } else {
                        tagFrequency.append(key + ": " + tags.get(key) + "\n");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to choose a file.");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fileSave(JTextArea tagFrequency) {
        String newFileName = JOptionPane.showInputDialog(frame, "Name the File:");

        if (newFileName == null) {
            return;
        } else if (newFileName.equals("") || newFileName.contains(" ")) {
            JOptionPane.showMessageDialog(frame, "That is not a viable name.");
            return;
        }

        File workingDirectory = new File(System.getProperty("user.dir"));
        Path file = Paths.get(workingDirectory.getPath() + "\\src\\" + newFileName + ".txt");

        try {
            OutputStream out =
                    new BufferedOutputStream(Files.newOutputStream(file, CREATE));
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(out));

            writer.write(tagFrequency.getText());

            writer.close();
            JOptionPane.showMessageDialog(frame, "Your file has been saved!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
