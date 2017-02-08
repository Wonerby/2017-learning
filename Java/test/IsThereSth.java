package com.demo.wonerby;

import java.io.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SearchStrUI {
    private static long totalFiles = 0;
    private static StringBuilder sb = new StringBuilder();

    @SuppressWarnings("resource")
    private static boolean doCheck(File file) {
        StringBuilder buffer = new StringBuilder();

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader br = new BufferedReader(inputStreamReader);
            String line = "";
            String reg = ".*(氪星)+.*";

            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }

            Pattern pat = Pattern.compile(reg);
            Matcher mat = pat.matcher(buffer.toString());

            if (mat.find()) {
                return true;
            }

            br.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static void exportToFile(StringBuilder sb) {
        File file = new File("C:\\Users\\Administrator\\Desktop\\doSearch".replace("\\", "/") + ".txt");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

            bw.write(sb.toString());
            bw.flush();
            bw.close();
        } catch (Exception e) {

            // TODO: handle exception
            System.out.println("------文件写入失败！------");
        }
    }

    private static void loadPage(String path) {
        File filePath = new File(path);
        File[] listFiles = filePath.listFiles();

        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".txt")) { // 扩展名为txt的文件
                        totalFiles++;
                        System.out.println(file);

                        if (doCheck(file)) {
                            System.out.println("文件-->" + file.getName() + "：有氪星！");
                            sb.append("文件名:").append(file.getName()).append("\t").append("路径:").append(file)
                                    .append("\r\n");
                            exportToFile(sb);
                        }
                    }
                } else {
                    loadPage(file.getPath());
                }
            }
        }
    }

    public static void main(String args[]) {
        System.out.println("------开始！------");

        String path = "X:\\\\xxx\\\\xxx\\\\xxx\\\\"; // 扫描路径
        path.replace("\\\\", "/");
        loadPage(path);
        System.out.println("------结束！------");
        System.out.println("总共扫描文件：" + totalFiles);
    }
}
