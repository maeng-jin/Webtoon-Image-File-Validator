import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileGroupValidator_temp {
    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            File[] fileList = directory.listFiles();

            // 에피소드(그룹 별) 파일 개수를 저장하는 맵
            HashMap<String, Integer> groupCountMap = new HashMap<>();

            for (File file : fileList) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    String group = extractGroupNumber(fileName);
                    int count = groupCountMap.getOrDefault(group, 0);
                    groupCountMap.put(group, count + 1);
                }
            }

            for (File file : fileList) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    String group = extractGroupNumber(fileName);
                    int expectedCount = extractExpectedCount(fileName);

                    int actualCount = groupCountMap.getOrDefault(group, 0);
                    if (actualCount == expectedCount) {
                        System.out.println(group + " | 정상 체크 | " + actualCount + " / " + expectedCount);
                    } else {
                        System.out.println(group + " | 이슈 발생 | " + actualCount + " / " + expectedCount);
                    }
                }
            }
        }
    }

    // 파일 이름에서 그룹 번호 추출
    public static String extractGroupNumber(String fileName) {
        String[] parts = fileName.split("_");

        if (parts.length == 3) {
            Pattern pattern = Pattern.compile("^(\\d+)"); // 그룹 번호 추출을 위한 정규 표현식 수정
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } else if (parts.length == 4) {
            Pattern pattern = Pattern.compile("^(\\d+_[^\\s_]+)"); // 그룹 번호 추출을 위한 정규 표현식 수정
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        // 기준 조건에 맞지 않는 경우 파일명 그대로 반환
        return fileName;
    }

    // 파일 이름에서 기대 파일 개수 추출
    private static int extractExpectedCount(String fileName) {
        int startIndex = fileName.lastIndexOf('(');
        int endIndex = fileName.lastIndexOf(')');
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String countStr = fileName.substring(startIndex + 1, endIndex);
            try {
                return Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
