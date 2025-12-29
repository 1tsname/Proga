import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class App extends JFrame {
    private JTextField filePathField;
    private JButton browseButton;
    private JRadioButton compressRadio;
    private JRadioButton decompressRadio;
    private JRadioButton compressFolderRadio;
    private JRadioButton decompressFolderRadio;
    private JRadioButton lz77Radio;
    private JRadioButton lz78Radio;
    private JButton processButton;
    private JButton exitButton;
    private JTextArea resultArea;
    private JButton clearResultsButton;
    private File selectedFile;
    private StringBuilder resultsHistory;
    
    public App() {
        resultsHistory = new StringBuilder();
        
        setTitle("LZ77/LZ78 Compression/Decompression Tool");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Панель выбора файла
        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePanel.setBorder(BorderFactory.createTitledBorder("Выберите файл или папку"));
        filePathField = new JTextField();
        filePathField.setEditable(false);
        browseButton = new JButton("Обзор...");
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(browseButton, BorderLayout.EAST);
        
        // Панель алгоритмов
        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        algorithmPanel.setBorder(BorderFactory.createTitledBorder("Алгоритм сжатия"));
        ButtonGroup algorithmGroup = new ButtonGroup();
        
        lz77Radio = new JRadioButton("LZ77", true);
        lz78Radio = new JRadioButton("LZ78");
        
        algorithmGroup.add(lz77Radio);
        algorithmGroup.add(lz78Radio);
        
        algorithmPanel.add(lz77Radio);
        algorithmPanel.add(lz78Radio);
        
        // Панель режимов
        JPanel modePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        modePanel.setBorder(BorderFactory.createTitledBorder("Режим работы"));
        ButtonGroup modeGroup = new ButtonGroup();
        
        compressRadio = new JRadioButton("Архивировать файл", true);
        decompressRadio = new JRadioButton("Разархивировать файл");
        compressFolderRadio = new JRadioButton("Архивировать папку");
        decompressFolderRadio = new JRadioButton("Разархивировать папку");
        
        modeGroup.add(compressRadio);
        modeGroup.add(decompressRadio);
        modeGroup.add(compressFolderRadio);
        modeGroup.add(decompressFolderRadio);
        
        modePanel.add(compressRadio);
        modePanel.add(decompressRadio);
        modePanel.add(compressFolderRadio);
        modePanel.add(decompressFolderRadio);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        processButton = new JButton("Выполнить");
        clearResultsButton = new JButton("Очистить");
        exitButton = new JButton("Выход");
        buttonPanel.add(processButton);
        buttonPanel.add(clearResultsButton);
        buttonPanel.add(exitButton);
        
        // Область вывода результата
        resultArea = new JTextArea(25, 80);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Результаты операций"));
        
        // Основная панель
        JPanel topPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        topPanel.add(filePanel);
        topPanel.add(algorithmPanel);
        topPanel.add(modePanel);
        topPanel.add(buttonPanel);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                
                // Определяем расширение файла в зависимости от алгоритма
                String algorithmExt = lz77Radio.isSelected() ? "lz77" : "lz78";
                String folderExt = lz77Radio.isSelected() ? "lz77f" : "lz78f";
                
                if (compressRadio.isSelected()) {
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
                    fileChooser.setDialogTitle("Выберите файл для архивации");
                } else if (decompressRadio.isSelected()) {
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Compressed Files", algorithmExt));
                    fileChooser.setDialogTitle("Выберите архив для распаковки");
                } else if (compressFolderRadio.isSelected()) {
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileChooser.setDialogTitle("Выберите папку для архивации");
                } else if (decompressFolderRadio.isSelected()) {
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Folder Archive", folderExt));
                    fileChooser.setDialogTitle("Выберите архив папки для распаковки");
                }
                
                int result = fileChooser.showOpenDialog(App.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile == null) {
                    JOptionPane.showMessageDialog(App.this,
                        "Сначала выберите файл или папку!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    addToHistory("=".repeat(100));
                    addToHistory("Операция начата: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    
                    String algorithm = lz77Radio.isSelected() ? "LZ77" : "LZ78";
                    addToHistory("Алгоритм: " + algorithm);
                    
                    if (compressRadio.isSelected()) {
                        if (!selectedFile.isFile()) {
                            JOptionPane.showMessageDialog(App.this,
                                "Выбранный объект не является файлом!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        addToHistory("Режим: Архивирование файла");
                        addToHistory("Файл: " + selectedFile.getName());
                        compressFile(selectedFile, algorithm);
                        
                    } else if (decompressRadio.isSelected()) {
                        if (!selectedFile.isFile()) {
                            JOptionPane.showMessageDialog(App.this,
                                "Выбранный объект не является файлом!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        addToHistory("Режим: Разархивирование файла");
                        addToHistory("Архив: " + selectedFile.getName());
                        decompressFile(selectedFile, algorithm);
                        
                    } else if (compressFolderRadio.isSelected()) {
                        if (!selectedFile.isDirectory()) {
                            JOptionPane.showMessageDialog(App.this,
                                "Выбранный объект не является папкой!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        addToHistory("Режим: Архивирование папки");
                        addToHistory("Папка: " + selectedFile.getName());
                        compressFolder(selectedFile, algorithm);
                        
                    } else if (decompressFolderRadio.isSelected()) {
                        if (!selectedFile.isFile()) {
                            JOptionPane.showMessageDialog(App.this,
                                "Выбранный объект не является файлом!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        addToHistory("Режим: Разархивирование папки");
                        addToHistory("Архив папки: " + selectedFile.getName());
                        decompressFolder(selectedFile, algorithm);
                    }
                    
                    addToHistory("Операция завершена: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    addToHistory("");
                    
                } catch (IOException ex) {
                    String errorMsg = "Ошибка ввода-вывода: " + ex.getMessage();
                    addToHistory("ОШИБКА: " + ex.getMessage());
                    ex.printStackTrace();
                } catch (Exception ex) {
                    String errorMsg = "Ошибка выполнения: " + ex.getMessage();
                    addToHistory("ОШИБКА ВЫПОЛНЕНИЯ: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        
        clearResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(App.this,
                    "Очистить историю результатов?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (response == JOptionPane.YES_OPTION) {
                    resultsHistory.setLength(0);
                    resultArea.setText("");
                    JOptionPane.showMessageDialog(App.this,
                        "История результатов очищена.",
                        "Информация",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
    
    private void addToHistory(String text) {
        resultsHistory.append(text).append("\n");
        resultArea.append(text + "\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }
    
    private void compressFile(File file, String algorithm) throws IOException {
        long originalSize = file.length();
        
        String input = Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
        String newPath;
        
        if (algorithm.equals("LZ77")) {
            List<Tag> compressedTags = LZ77.compress(input);
            addToHistory("Сгенерировано тегов LZ77: " + compressedTags.size());
            
            byte[] compressedBytes = Tag.convertTagsToBits(compressedTags).toByteArray();
            newPath = file.getPath() + ".lz77";
            File compressedFile = new File(newPath);
            compressedFile.createNewFile();
            Files.write(compressedFile.toPath(), compressedBytes);
            
            addToHistory("Размер сжатых данных: " + compressedBytes.length + " байт");
        } else {
            LZ78 lz78 = new LZ78();
            List<LZ78.LZ78Pair> compressedPairs = lz78.compress(input);
            
            newPath = file.getPath() + ".lz78";
            File compressedFile = new File(newPath);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(compressedFile))) {
                for (LZ78.LZ78Pair pair : compressedPairs) {
                    writer.println(pair.index + ":" + (int)pair.nextChar);
                }
            }
            
            addToHistory("Сгенерировано пар LZ78: " + compressedPairs.size());
        }
        
        File compressedFile = new File(newPath);
        long compressedSize = compressedFile.length();
        
        addToHistory("\n=== Результаты сжатия файла (" + algorithm + ") ===");
        addToHistory("Исходный размер: " + originalSize + " байт (" + input.length() + " символов)");
        addToHistory("Размер архива: " + compressedSize + " байт");
        
        if (originalSize > 0) {
            double compressionRatio = (1 - (double)compressedSize / originalSize) * 100;
            addToHistory("Коэффициент сжатия: " + String.format("%.2f%%", compressionRatio));
        }
        
        addToHistory("\nАрхив сохранен как: " + newPath);
    }
    
    private void decompressFile(File file, String algorithm) throws IOException {
        long compressedSize = file.length();
        String decompressedTxt;
        String fileExtension = getFileExtension(file);
        
        if (algorithm == null) {
            algorithm = fileExtension.equals("lz77") ? "LZ77" : "LZ78";
            addToHistory("Автоопределен алгоритм: " + algorithm);
        }
        
        if (algorithm.equals("LZ77") || fileExtension.equals("lz77")) {
            byte[] input = Files.readAllBytes(file.toPath());
            BitSet compressedBits = BitSet.valueOf(input);
            List<Tag> tags = Tag.convertBitsToTags(compressedBits);
            
            addToHistory("Прочитано тегов LZ77: " + tags.size());
            decompressedTxt = LZ77.decompress(tags);
        } else {
            LZ78 lz78 = new LZ78();
            List<LZ78.LZ78Pair> pairs = new ArrayList<>();
            
            List<String> lines = Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        int index = Integer.parseInt(parts[0]);
                        int charCode = Integer.parseInt(parts[1]);
                        char nextChar = (char) charCode;
                        pairs.add(new LZ78.LZ78Pair(index, nextChar));
                    }
                }
            }
            
            addToHistory("Прочитано пар LZ78: " + pairs.size());
            decompressedTxt = lz78.decompress(pairs);
        }
        
        String baseName = file.getName();
        if (baseName.endsWith(".lz77") || baseName.endsWith(".lz78")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }
        
        String newPath = file.getParent() + File.separator + baseName + "_decompressed.txt";
        File decompressedFile = new File(newPath);
        
        int counter = 1;
        while (decompressedFile.exists()) {
            newPath = file.getParent() + File.separator + baseName + "_decompressed_" + counter + ".txt";
            decompressedFile = new File(newPath);
            counter++;
        }
        
        try (FileOutputStream fos = new FileOutputStream(decompressedFile)) {
            fos.write(decompressedTxt.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        
        long decompressedSize = decompressedFile.length();
        
        addToHistory("\n=== Результаты распаковки файла (" + algorithm + ") ===");
        addToHistory("Размер архива: " + compressedSize + " байт");
        addToHistory("Размер распакованного файла: " + decompressedTxt.length() + 
                    " символов (" + decompressedSize + " байт)");
        addToHistory("Файл сохранен как: " + newPath);
        
        if (decompressedTxt.length() == 0) {
            addToHistory("ПРЕДУПРЕЖДЕНИЕ: Распакованный файл пуст!");
        }
    }
    
    private void compressFolder(File folder, String algorithm) throws IOException {
        List<File> textFiles = new ArrayList<>();
        findTextFiles(folder, textFiles);
        
        if (textFiles.isEmpty()) {
            addToHistory("В папке не найдено текстовых файлов (.txt)!");
            return;
        }
        
        addToHistory("Найдено текстовых файлов: " + textFiles.size());
        long totalOriginalSize = 0;
        
        String extension = algorithm.equals("LZ77") ? "lz77f" : "lz78f";
        String archivePath = folder.getPath() + "." + extension;
        File archiveFile = new File(archivePath);
        
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archiveFile));
        
        ZipEntry infoEntry = new ZipEntry("_folder_info.txt");
        zos.putNextEntry(infoEntry);
        String info = algorithm + " Folder Archive\n" +
                    "Algorithm: " + algorithm + "\n" +
                    "Original folder: " + folder.getName() + "\n" +
                    "Number of files: " + textFiles.size() + "\n" +
                    "Created: " + new Date() + "\n";
        zos.write(info.getBytes());
        zos.closeEntry();
        
        for (File textFile : textFiles) {
            long fileSize = textFile.length();
            totalOriginalSize += fileSize;
            
            addToHistory("\nАрхивация файла: " + textFile.getName() + " (" + fileSize + " байт)");
            
            String content = Files.readString(textFile.toPath());
            byte[] compressedBytes;
            
            if (algorithm.equals("LZ77")) {
                List<Tag> compressedTags = LZ77.compress(content);
                compressedBytes = Tag.convertTagsToBits(compressedTags).toByteArray();
                addToHistory("  тегов LZ77: " + compressedTags.size());
            } else {
                LZ78 lz78 = new LZ78();
                List<LZ78.LZ78Pair> compressedPairs = lz78.compress(content);
                
                StringBuilder pairsBuilder = new StringBuilder();
                for (LZ78.LZ78Pair pair : compressedPairs) {
                    pairsBuilder.append(pair.index).append(":").append((int)pair.nextChar).append("\n");
                }
                compressedBytes = pairsBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                
                addToHistory("  пар LZ78: " + compressedPairs.size());
            }
            
            String relativePath = getRelativePath(folder, textFile);
            ZipEntry entry = new ZipEntry(relativePath);
            zos.putNextEntry(entry);
            
            zos.write(intToBytes(compressedBytes.length));
            zos.write(compressedBytes);
            
            zos.closeEntry();
            
            addToHistory("  сжатых байт: " + compressedBytes.length);
        }
        
        zos.close();
        
        long archiveSize = archiveFile.length();
        
        addToHistory("\n=== Результаты архивации папки (" + algorithm + ") ===");
        addToHistory("Количество файлов: " + textFiles.size());
        addToHistory("Общий исходный размер: " + totalOriginalSize + " байт");
        addToHistory("Размер архива папки: " + archiveSize + " байт");
        if (totalOriginalSize > 0) {
            addToHistory("Общий коэффициент сжатия: " + String.format("%.2f%%", 
                (1 - (double)archiveSize / totalOriginalSize) * 100));
        }
        
        addToHistory("\nАрхив папки сохранен как: " + archivePath);
    }
    
    private void decompressFolder(File archiveFile, String algorithm) throws IOException {
        long archiveSize = archiveFile.length();
        addToHistory("Размер архива: " + archiveSize + " байт\n");
        
        String fileExtension = getFileExtension(archiveFile);
        String detectedAlgorithm = fileExtension.equals("lz77f") ? "LZ77" : "LZ78";
        if (algorithm == null) {
            algorithm = detectedAlgorithm;
            addToHistory("Автоопределен алгоритм: " + algorithm);
        }
        
        String archiveName = archiveFile.getName();
        String folderName = archiveName.substring(0, archiveName.lastIndexOf('.'));
        File outputFolder = new File(archiveFile.getParentFile(), folderName + "_decompressed");
        
        int counter = 1;
        while (outputFolder.exists()) {
            outputFolder = new File(archiveFile.getParentFile(), folderName + "_decompressed_" + counter);
            counter++;
        }
        
        if (!outputFolder.mkdirs()) {
            throw new IOException("Не удалось создать папку для распаковки: " + outputFolder.getAbsolutePath());
        }
        
        int fileCount = 0;
        int totalChars = 0;
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archiveFile))) {
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                if (entryName.equals("_folder_info.txt")) {
                    byte[] infoBytes = zis.readAllBytes();
                    String info = new String(infoBytes, java.nio.charset.StandardCharsets.UTF_8);
                    addToHistory("Информация об архиве:\n" + info);
                    zis.closeEntry();
                    continue;
                }
                
                fileCount++;
                
                byte[] sizeBytes = new byte[4];
                int bytesRead = zis.read(sizeBytes);
                if (bytesRead != 4) {
                    throw new IOException("Не удалось прочитать размер данных для файла: " + entryName);
                }
                
                int compressedSize = bytesToInt(sizeBytes);
                byte[] compressedBytes = new byte[compressedSize];
                
                int totalRead = 0;
                while (totalRead < compressedSize) {
                    int read = zis.read(compressedBytes, totalRead, compressedSize - totalRead);
                    if (read == -1) {
                        throw new IOException("Неожиданный конец файла при чтении: " + entryName);
                    }
                    totalRead += read;
                }
                
                String decompressedContent;
                
                if (algorithm.equals("LZ77")) {
                    BitSet compressedBits = BitSet.valueOf(compressedBytes);
                    List<Tag> tags = Tag.convertBitsToTags(compressedBits);
                    decompressedContent = LZ77.decompress(tags);
                } else {
                    LZ78 lz78 = new LZ78();
                    String pairContent = new String(compressedBytes, java.nio.charset.StandardCharsets.UTF_8);
                    List<LZ78.LZ78Pair> pairs = new ArrayList<>();
                    
                    String[] lines = pairContent.split("\n");
                    for (String line : lines) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            String[] parts = line.split(":");
                            if (parts.length == 2) {
                                int index = Integer.parseInt(parts[0]);
                                int charCode = Integer.parseInt(parts[1]);
                                char nextChar = (char) charCode;
                                pairs.add(new LZ78.LZ78Pair(index, nextChar));
                            }
                        }
                    }
                    
                    decompressedContent = lz78.decompress(pairs);
                }
                
                File outputFile = new File(outputFolder, entryName);
                outputFile.getParentFile().mkdirs();
                
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(decompressedContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                
                totalChars += decompressedContent.length();
                addToHistory("Распакован файл: " + entryName + " (" + 
                        decompressedContent.length() + " символов)");
                
                zis.closeEntry();
            }
        }
        
        addToHistory("\n=== Результаты распаковки архива папки (" + algorithm + ") ===");
        addToHistory("Распаковано файлов: " + fileCount);
        addToHistory("Всего символов: " + totalChars);
        addToHistory("Папка создана: " + outputFolder.getAbsolutePath());
        
        File[] decompressedFiles = outputFolder.listFiles();
        if (decompressedFiles != null && decompressedFiles.length > 0) {
            addToHistory("\nСодержимое распакованной папки:");
            for (File file : decompressedFiles) {
                if (!file.getName().startsWith("_")) {
                    addToHistory("  - " + file.getName() + " (" + file.length() + " байт)");
                }
            }
        }
    }
    
    private void findTextFiles(File folder, List<File> textFiles) {
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                findTextFiles(file, textFiles);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                textFiles.add(file);
            }
        }
    }
    
    private String getRelativePath(File base, File file) {
        String basePath = base.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        
        if (filePath.startsWith(basePath)) {
            return filePath.substring(basePath.length() + 1);
        }
        return file.getName();
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf + 1);
    }
    
    private byte[] intToBytes(int value) {
        return new byte[] {
            (byte)(value >> 24),
            (byte)(value >> 16),
            (byte)(value >> 8),
            (byte)value
        };
    }
    
    private int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                App app = new App();
                app.setVisible(true);
            }
        });
    }
}