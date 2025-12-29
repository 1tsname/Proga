import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TestGenerator {
    
    public static void main(String[] args) {
        String testDir = "test_files";
        new File(testDir).mkdirs();
        
        System.out.println("Генерация тестовых файлов...\n");
        
        // 1. Текст с повторениями (хорошо сжимается LZ77)
        generateRepeatedText(testDir + "/repeated.txt", "ABAB", 100);
        
        // 2. Случайный текст (плохо сжимается)
        generateRandomText(testDir + "/random.txt", 500);
        
        // 3. Текст с длинными повторениями
        generateLongRepeatedText(testDir + "/long_repeat.txt", "XYZ", 50);
        
        // 4. Текст с английскими словами
        generateEnglishText(testDir + "/english.txt");
        
        // 6. Маленький файл (граничный случай)
        generateSmallFile(testDir + "/small.txt", "TEST");
        
        // 7. Файл с одним символом много раз
        generateSingleChar(testDir + "/single_char.txt", 'X', 300);
        
        // 8. Файл с паттернами
        generatePatternText(testDir + "/pattern.txt", "ABCD", 30);
        
        // 9. Текст со спецсимволами
        generateSpecialChars(testDir + "/special.txt", 150);
        
        System.out.println("\nВсе тестовые файлы созданы в папке: " + testDir);
        System.out.println("\nДля тестирования сжатия используйте:");
        System.out.println("java LZ77 c test_files/repeated.txt");
        System.out.println("\nДля тестирования распаковки сначала сожмите файл,");
        System.out.println("затем используйте:");
        System.out.println("java LZ77 d test_files/repeated.txt.lz77");
    }
    
    // 1. Текст с короткими повторениями
    private static void generateRepeatedText(String filename, String pattern, int repetitions) {
        try (FileWriter writer = new FileWriter(filename)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < repetitions; i++) {
                sb.append(pattern);
                if (i % 15 == 14) sb.append("\n");
            }
            writer.write(sb.toString());
            System.out.println("Создан: " + filename + " (" + sb.length() + " символов)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 2. Случайный текст
    private static void generateRandomText(String filename, int length) {
        try (FileWriter writer = new FileWriter(filename)) {
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                char c = (char) (random.nextInt(94) + 32); // Печатные ASCII символы
                sb.append(c);
                if (i % 60 == 59) sb.append("\n");
            }
            writer.write(sb.toString());
            System.out.println("Создан: " + filename + " (" + sb.length() + " символов)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 3. Длинные повторения
    private static void generateLongRepeatedText(String filename, String pattern, int repetitions) {
        try (FileWriter writer = new FileWriter(filename)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < repetitions; i++) {
                // Повторяем паттерн 5-15 раз
                int repeat = 5 + (int)(Math.random() * 10);
                sb.append(pattern.repeat(repeat));
                if (i % 5 == 4) sb.append("\n");
            }
            writer.write(sb.toString());
            System.out.println("Создан: " + filename + " (" + sb.length() + " символов)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 4. Английский текст (короткий словарь)
    private static void generateEnglishText(String filename) {
        String[] words = {"hello", "world", "test", "data", "file", "text", "compression", 
                          "algorithm", "java", "program", "lz77", "code", "example"};
        
        try (FileWriter writer = new FileWriter(filename)) {
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            
            // Генерируем 100-150 слов
            int wordCount = 100 + random.nextInt(50);
            for (int i = 0; i < wordCount; i++) {
                sb.append(words[random.nextInt(words.length)]);
                sb.append(" ");
                if (i % 10 == 9) sb.append("\n");
            }
            writer.write(sb.toString());
            System.out.println("Создан: " + filename + " (" + sb.length() + " символов)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 6. Маленький файл
    private static void generateSmallFile(String filename, String text) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(text);
            System.out.println("Создан: " + filename + " (" + text.length() + " символов)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 7. Файл с одним символом
    private static void generateSingleChar(String filename, char c, int count) {
        try (FileWriter writer = new FileWriter(filename)) {
            String text = String.valueOf(c).repeat(count);
            writer.write(text);
            System.out.println("Создан: " + filename + " (" + text.length() + " символов)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 8. Файл с паттернами
    private static void generatePatternText(String filename, String pattern, int repetitions) {
        try (FileWriter writer = new FileWriter(filename)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < repetitions; i++) {
                sb.append(pattern);
                // Добавляем немного вариаций
                if (Math.random() > 0.7) sb.append(" ");
                if (i % 8 == 7) sb.append("\n");
            }
            writer.write(sb.toString());
            System.out.println("Создан: " + filename + " (" + sb.length() + " символов)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 9. Текст со спецсимволами
    private static void generateSpecialChars(String filename, int length) {
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?/~`\"'\\";
        
        try (FileWriter writer = new FileWriter(filename)) {
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < length; i++) {
                if (random.nextBoolean()) {
                    // Спецсимвол
                    sb.append(specialChars.charAt(random.nextInt(specialChars.length())));
                } else {
                    // Буква или цифра
                    if (random.nextBoolean()) {
                        sb.append((char) (random.nextInt(26) + 'A'));
                    } else {
                        sb.append((char) (random.nextInt(26) + 'a'));
                    }
                }
                if (i % 40 == 39) sb.append("\n");
            }
            
            writer.write(sb.toString());
            System.out.println("Создан: " + filename + " (" + sb.length() + " символов)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}