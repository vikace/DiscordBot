import java.util.HashMap;
import java.io.*;
public class Main{
public static void main(String args[])throws IOException
{
File file=new File("E:\\CapnCook\\src\\main\\resources\\score.txt");
ObjectOutputStream ois;
     
            ois=new ObjectOutputStream(new FileOutputStream(file));
            ois.writeObject(new HashMap<String,Integer>());
            ois.close();
}
}