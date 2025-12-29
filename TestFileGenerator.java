import java.io.*;
import java.util.Random;

public class TestFileGenerator {
    
    public static void main(String[] args) {
        try {
            String[] patterns = {
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                "abcdefghijklmnopqrstuvwxyz",
                "0123456789",
                "!@#$%^&*()_+-=[]{}|;:,.<>?",
                "REPEATING_TEXT_FOR_TEST_",
                "DATA_COMPRESSION_TEST_PATTERN_",
                "LOREM_IPSUM_DOLOR_SIT_AMET_",
                "THE_QUICK_BROWN_FOX_JUMPS_",
                "PACK_MY_BOX_WITH_FIVE_DOZEN_"
            };
            
            generateFile("test_100_chars.txt", 100, patterns);
            generateFile("test_1000_chars.txt", 1000, patterns);
            generateFile("test_10000_chars.txt", 10000, patterns);
            
            System.out.println("Files successfully created!");
            System.out.println("test_100_chars.txt - 100 characters");
            System.out.println("test_1000_chars.txt - 1000 characters");
            System.out.println("test_10000_chars.txt - 10000 characters");
            
        } catch (IOException e) {
            System.err.println("Error creating files: " + e.getMessage());
        }
    }
    
    private static void generateFile(String filename, int targetSize, String[] patterns) 
            throws IOException {
        
        Random random = new Random(42);
        StringBuilder content = new StringBuilder();
        
        String mainPattern = "DATA_FOR_COMPRESSION_REPEATS_";
        content.append(mainPattern.repeat(5));
        
        while (content.length() < targetSize) {
            if (random.nextDouble() < 0.6) {
                String pattern = patterns[random.nextInt(patterns.length)];
                int repeatCount = 1 + random.nextInt(5);
                content.append(pattern.repeat(repeatCount));
                int length = 10 + random.nextInt(50);
                for (int i = 0; i < length && content.length() < targetSize; i++) {
                    int charType = random.nextInt(3);
                    char c;
                    switch (charType) {
                        case 0:
                            c = (char) ('A' + random.nextInt(26));
                            break;
                        case 1:
                            c = (char) ('a' + random.nextInt(26));
                            break;
                        case 2:
                            c = (char) ('0' + random.nextInt(10));
                            break;
                        default:
                            c = ' ';
                    }
                    content.append(c);
                }
            }
            
            if (random.nextDouble() < 0.3 && content.length() < targetSize) {
                String[] punctuations = { " ", ", ", ". ", "! ", "? ", "; ", ": " };
                content.append(punctuations[random.nextInt(punctuations.length)]);
            }
        }
        String result = content.substring(0, targetSize);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(result);
        }
        
        System.out.println("\nCreated file: " + filename);
        System.out.println("Size: " + result.length() + " characters");
        System.out.println("File beginning sample: " + result.substring(0, Math.min(100, result.length())) + "...");
        
        analyzeRepetitions(result, filename);
    }
    
    private static void analyzeRepetitions(String text, String filename) {
        System.out.println("Repetition analysis for " + filename + ":");
        
        String testPattern = "DATA_FOR_COMPRESSION_REPEATS_";
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(testPattern, index)) != -1) {
            count++;
            index += testPattern.length();
        }
        
        System.out.println("  - Pattern '" + testPattern.substring(0, 10) + "...' appears " + count + " times");
        
        System.out.println("  - Examples of repeated sequences:");
        findRepeatedSubstrings(text, 10, 3);
        
        System.out.println();
    }
    
    private static void findRepeatedSubstrings(String text, int minLength, int maxExamples) {
        int examplesFound = 0;
        
        for (int len = minLength; len <= 20 && examplesFound < maxExamples; len++) {
            for (int i = 0; i <= text.length() - len && examplesFound < maxExamples; i++) {
                String substring = text.substring(i, i + len);
                int count = countOccurrences(text, substring);
                
                if (count >= 3 && !substring.trim().isEmpty() && !substring.matches("\\s+")) {
                    System.out.println("    * '" + substring + "' - " + count + " repeats");
                    examplesFound++;
                    
                    i += len;
                }
            }
        }
        
        if (examplesFound == 0) {
            System.out.println("    * No explicit repetitions of " + minLength + "+ characters found");
        }
    }
    
    private static int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        
        return count;
    }
}