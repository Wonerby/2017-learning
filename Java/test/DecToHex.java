import java.util.Scanner;

/**
 * Created by Wonerby on 2017/2/6 0006.
 */
public class DecToHex {
    public static void main(String[] args) {
        boolean flag = true;
        Scanner sc   = new Scanner(System.in);

        while (flag) {
            System.out.println("请出入一个十进制数");

            try {
                int num = sc.nextInt();

                toHex(num);
                System.out.println(Integer.toHexString(num));    // java自带的转换十六进制方法
            } catch (Exception e) {
                System.out.println("然而你并没有输入十进制数！");
            }

            System.out.println("继续？");
            System.out.println("输入Y/y或N/n");
            sc.nextLine();

            String nextLine = sc.nextLine();

            if (!("Y".equals(nextLine) || "y".equals(nextLine) || "N".equals(nextLine) || "n".equals(nextLine))) {
                System.out.println("然而你并没有输入Y/y或N/n！");
            }

            flag = "Y".equals(nextLine) || "y".equals(nextLine);

            String str = flag
                         ? "继续！"
                         : "结束！";

            System.out.println(str);
        }

        sc.close();
    }

    public static void toHex(int num) {
        if (num == 0) {
            System.out.println('0');

            return;
        }

        char[] chs = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        char[] arr = new char[8];
        int    pos = arr.length;

        while (num != 0) {
            arr[--pos] = chs[num & 15];
            num        = num >>> 4;
        }

        for (int i = pos; i < arr.length; i++) {
            System.out.print(arr[i]);
        }

        System.out.println();
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
