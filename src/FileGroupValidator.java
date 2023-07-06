import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import javax.swing.JFileChooser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileGroupValidator {
    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            String outputFolderName = directory.getName();

            File[] fileList = directory.listFiles();

            // 그룹 별 파일 개수를 저장하는 맵
            HashMap<String, Integer> groupCountMap = new HashMap<>();

            for (File file : fileList) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    String group = extractGroupNumber(fileName);
                    int count = groupCountMap.getOrDefault(group, 0);
                    groupCountMap.put(group, count + 1);
                }
            }

            // 폴더 생성
            File outputFolder = new File(directory.getAbsolutePath() + "/" + outputFolderName + "/img/");
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            // HTML 파일 생성을 위한 디렉토리 생성
            File htmlFolder = new File(directory.getAbsolutePath() + "/" + outputFolderName + "/html/");
            if (!htmlFolder.exists()) {
                htmlFolder.mkdirs();
            }

            // 인덱스 파일 생성
            try {
                File indexFile = new File(directory.getAbsolutePath() + "/" + outputFolderName + "/index.html");
                FileWriter writer = new FileWriter(indexFile);

                // HTML 파일 리스트 생성
                StringBuilder htmlList = new StringBuilder();
                String prevGroup = "";
                String nextGroup = "";
                for (File file : fileList) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        String group = extractGroupNumber(fileName);
                        int expectedCount = extractExpectedCount(fileName);

                        int actualCount = groupCountMap.getOrDefault(group, 0);
                        if (actualCount != expectedCount) {
                            System.out.println(group + " | 이슈 발생 | " + actualCount + " / " + expectedCount);
                        } else {
                            // 그룹별 폴더 생성
                            File groupFolder = new File(outputFolder.getAbsolutePath() + "/" + group);
                            if (!groupFolder.exists()) {
                                groupFolder.mkdirs();
                            }

                            // 파일 복사
                            File destinationFile = new File(groupFolder.getAbsolutePath() + "/" + fileName);
                            Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                            // HTML 파일 생성
                            String htmlFileName = group + ".html";

                            // HTML 파일 리스트에 추가
                            if (!group.equals(prevGroup)) {
                                htmlList.append("<li><a href=\"html/").append(htmlFileName).append("\">Episode ").append(group).append("</a></li>");
                            }

                            if (group.equals(prevGroup)) {
                                nextGroup = group;
                            }

                            File htmlFile = new File(htmlFolder.getAbsolutePath() + "/" + htmlFileName);
                            FileWriter htmlWriter = new FileWriter(htmlFile);
                            htmlWriter.write("<html><head>");
                            htmlWriter.write("<style>img { display: block; max-width: 100%; height: auto; margin: 0 auto; }"); // 이미지 중앙 정렬
                            htmlWriter.write(".fixed-buttons { position: fixed; bottom: 0; left: 0; right: 0; padding: 10px; text-align: center; }"); // 버튼 고정 스타일 및 가운데 정렬
                            htmlWriter.write(".fixed-buttons button { margin: 0 5px; }</style>"); // 버튼 간격 추가
                            htmlWriter.write("</head><body>"); // Move the opening <body> tag here
                            htmlWriter.write("<script>");
                            htmlWriter.write("function loadImagesOnScroll() {");
                            htmlWriter.write("var images = document.querySelectorAll('img[data-src]');");
                            htmlWriter.write("var screenHeight = window.innerHeight + 200;");
                            htmlWriter.write("var imageLimit = 3;");
                            htmlWriter.write("for (var i = 0; i < images.length; i++) {");
                            htmlWriter.write("if (i >= imageLimit) { break; }");
                            htmlWriter.write("var rect = images[i].getBoundingClientRect();");
                            htmlWriter.write("if (rect.bottom <= screenHeight) {");
                            htmlWriter.write("images[i].src = images[i].getAttribute('data-src');");
                            htmlWriter.write("images[i].removeAttribute('data-src');"); // data-src 속성 제거
                            htmlWriter.write("}");
                            htmlWriter.write("}");
                            htmlWriter.write("}");
                            htmlWriter.write("function moveToPreviousEpisode() {");
                            htmlWriter.write("var currentEpisode = window.location.pathname.split('/').pop().split('.').shift();");
                            htmlWriter.write("var previousEpisode = Number(currentEpisode) - 1;");
                            htmlWriter.write("window.location.href = previousEpisode.toString().padStart(3, '0') + '.html';");
                            htmlWriter.write("}");
                            htmlWriter.write("function moveToNextEpisode() {");
                            htmlWriter.write("var currentEpisode = window.location.pathname.split('/').pop().split('.').shift();");
                            htmlWriter.write("var nextEpisode = Number(currentEpisode) + 1;");
                            htmlWriter.write("var nextEpisodeUrl = nextEpisode.toString().padStart(3, '0') + '.html';");
                            htmlWriter.write("window.location.href = nextEpisodeUrl;");
                            htmlWriter.write("}");
                            htmlWriter.write("window.addEventListener('scroll', function () {");
                            htmlWriter.write("loadImagesOnScroll();");
                            htmlWriter.write("scrollEnd();");
                            htmlWriter.write("});");
                            htmlWriter.write("function scrollEnd(){");
                            htmlWriter.write("var scrollHeight = document.compatMode==\"CSS1Compat\"? document.documentElement.scrollHeight : document.body.scrollHeight;");
                            htmlWriter.write("var clientHeight = document.compatMode==\"CSS1Compat\"? document.documentElement.clientHeight : document.body.clientHeight;");
                            htmlWriter.write("var ScrollTop = document.compatMode == \"CSS1Compat\"? document.documentElement.scrollTop : document.body.scrollTop;");
                            htmlWriter.write("var scrollPos = scrollHeight - ScrollTop;");
                            htmlWriter.write("if (clientHeight == scrollPos) {");
                            htmlWriter.write("document.getElementsByClassName('fixed-buttons')[0].style.display = 'block';");
                            htmlWriter.write("} else {");
                            htmlWriter.write("document.getElementsByClassName('fixed-buttons')[0].style.display = 'none';}");
                            htmlWriter.write("}");
                            htmlWriter.write("window.onload = function(){");
                            htmlWriter.write("loadImagesOnScroll();");
                            htmlWriter.write("document.getElementsByClassName('fixed-buttons')[0].style.display = 'none';");
                            htmlWriter.write("}");
                            htmlWriter.write("</script>");
                            // 이미지 파일 경로 추가
                            File imgFolder = new File(outputFolder.getAbsolutePath() + "/" + group);
                            File[] imgFiles = imgFolder.listFiles();
                            for (File imgFile : imgFiles) {
                                if (imgFile.isFile()) {
                                    htmlWriter.write("<img data-src=\"" + imgFile.getAbsolutePath().replace("\\", "/") + "\">");
                                    // htmlWriter.write("<br/>"); // 개행 추가
                                }
                            }

                            htmlWriter.write("<div class=\"fixed-buttons\">"); // Add the fixed-buttons class to the div
                            htmlWriter.write("<button onclick=\"moveToPreviousEpisode()\">이전</button>");
                            htmlWriter.write("<button onclick=\"moveToNextEpisode()\">다음</button>");
                            htmlWriter.write("</div>");
                            htmlWriter.write("</body></html>");
                            htmlWriter.close();

                            // HTML 파일 리스트에 추가
                            if (!group.equals(prevGroup)) {
                                htmlList.append("<li><a href=\"html/").append(htmlFileName).append("\">Episode ").append(group).append("</a></li>");
                            }

                            prevGroup = group;
                        }
                    }
                }

                // 인덱스 파일 작성
                writer.write("<html><body><h1>Index</h1><ul>");
                writer.write(htmlList.toString());
                writer.write("</ul></body></html>");

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
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
