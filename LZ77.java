import java.util.ArrayList;
import java.util.List;

public class LZ77 {
    public static List<Tag> compress(String str) {
        List<Tag> result = new ArrayList<>();
        int searchBufferSize = (int) Math.pow(2, Tag.bufferBitLength) - 1;
        int lookAheadBufferSize = searchBufferSize;
        
        int i = 0;
        while (i < str.length()) {
            int searchStart = Math.max(0, i - searchBufferSize);
            String searchBuffer = str.substring(searchStart, i);
            int matchLength = 0;
            int matchDistance = 0;
            char nextChar = 0;
            
            for (int len = 1; len <= Math.min(lookAheadBufferSize, str.length() - i); len++) {
                String lookAhead = str.substring(i, i + len);
                int pos = searchBuffer.lastIndexOf(lookAhead);
                if (pos != -1) {
                    matchLength = len;
                    matchDistance = searchBuffer.length() - pos;
                    if (i + len < str.length()) {
                        nextChar = str.charAt(i + len);
                    } else {
                        nextChar = 0;
                    }
                } else {
                    break;
                }
            }
            if (matchLength > 0) {
                result.add(new Tag(matchDistance, matchLength, nextChar));
                i += matchLength + (nextChar != 0 ? 1 : 0);
            } else {
                result.add(new Tag(0, 0, str.charAt(i)));
                i++;
            }
        }
        return result;
    }

    public static String decompress(List<Tag> tags) {
        StringBuilder builder = new StringBuilder();
        for (Tag tag : tags) {
            int distance = (int) tag.getPosition();
            int length = (int) tag.getLength();
            char nextChar = tag.getNextChar();
            if (distance == 0 && length == 0) {
                if (nextChar != 0) {
                    builder.append(nextChar);
                }
            } else if (distance > 0 && length > 0) {
                int startPos = builder.length() - distance;
                if (startPos < 0 || startPos >= builder.length()) {
                    throw new IllegalArgumentException("Invalid distance in tag");
                }
                for (int i = 0; i < length; i++) {
                    builder.append(builder.charAt(startPos + i));
                }
                if (nextChar != 0) {
                    builder.append(nextChar);
                }
            } else {
                throw new IllegalArgumentException("Invalid tag: distance=" + distance + ", length=" + length);
            }
        }
        return builder.toString();
    }
}