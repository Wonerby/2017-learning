import java.util.Random;

/**
 * Created by Wonerby on 2017/2/6 0006.
 */
public class RandomNum {
    public static void main(String[] args) {
        int     randomInt        = new Random().nextInt();
        long    randomLong       = new Random().nextLong();
        float   randomFloat      = new Random().nextFloat();
        double  randomDouble     = new Random().nextDouble();
        boolean randomBool       = new Random().nextBoolean();
        double  randomSmallFloat = Math.random();

        // 产生随机整数
        System.out.println("产生随机整数,就决定是你了：" + randomInt);

        // 产生随机大整数
        System.out.println("产生随机大整数,就决定是你了：" + randomLong);

        // 产生随机浮点数
        System.out.println("产生随机浮点数,就决定是你了：" + randomFloat);

        // 产生随机双精度浮点数
        System.out.println("产生随机双精度浮点数,就决定是你了：" + randomDouble);

        // 产生随机布尔数
        System.out.println("产生随机布尔数,就决定是你了：" + randomBool);

        // 产生随机0~1小数
        System.out.println("产生随机0~1小数,就决定是你了：" + randomSmallFloat);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
