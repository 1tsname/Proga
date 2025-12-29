import java.util.*;

public class LZ78 {
    
    public static class LZ78Pair {
        int index;
        char nextChar;
        
        public LZ78Pair(int index, char nextChar) {
            this.index = index;
            this.nextChar = nextChar;
        }
        
        @Override
        public String toString() {
            return "(" + index + "," + nextChar + ")";
        }
    }
    
    private float ratio;
    
    public List<LZ78Pair> compress(String input) {
        List<LZ78Pair> result = new ArrayList<>();
        Map<String, Integer> dict = new HashMap<>();
        String current = "";
        int nextId = 1;
        
        dict.put("", 0);
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String phrase = current + c;
            
            if (dict.containsKey(phrase)) {
                current = phrase;
            } else {
                int idx = dict.get(current);
                result.add(new LZ78Pair(idx, c));
                
                dict.put(phrase, nextId++);
                current = "";
            }
        }
        
        if (!current.isEmpty()) {
            if (dict.containsKey(current)) {
                result.add(new LZ78Pair(dict.get(current), '\0'));
            } else {
                char last = current.charAt(current.length() - 1);
                String prefix = current.substring(0, current.length() - 1);
                int idx = dict.getOrDefault(prefix, 0);
                result.add(new LZ78Pair(idx, last));
            }
        }
        
        ratio = result.size() > 0 ? (float) input.length() / result.size() : 0;
        
        return result;
    }
    
    public String decompress(List<LZ78Pair> compressed) {
        StringBuilder result = new StringBuilder();
        List<String> dict = new ArrayList<>();
        dict.add("");
        
        for (LZ78Pair pair : compressed) {
            String phrase;
            
            if (pair.index >= dict.size()) {
                phrase = "";
            } else {
                phrase = dict.get(pair.index);
            }
            
            if (pair.nextChar != 0) {
                phrase = phrase + pair.nextChar;
            }
            
            result.append(phrase);
            
            if (!phrase.isEmpty()) {
                dict.add(phrase);
            }
        }
        
        return result.toString();
    }
    
    public float getRatio() {
        return ratio;
    }
}