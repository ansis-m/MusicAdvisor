import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Pages {


    private static int pageSize;
    private static List<String> output;
    private static int size;
    private static int currentIndex;
    private static boolean next;



    Pages(int pageSize){
        this.pageSize = pageSize;
        currentIndex = 0;
        output = new ArrayList<>();
        next = true;
    }

    public static void addOutput(String entry){
        output.add(entry);
        size++;
    }

    public static void clear() {
        output.clear();
        currentIndex = 0;
        size = 0;
        next = true;
    }

    public static void displayNext() {

        System.out.println();

        if(!next) {
            currentIndex = currentIndex + pageSize >= size? size : currentIndex + pageSize;
            next = true;
        }

        if(currentIndex == size) {
            System.out.println("No more pages.");
            return;
        }

        int end = currentIndex + pageSize >= size? size : currentIndex + pageSize;

        for(int i = currentIndex; i < end; i++)
            System.out.println(output.get(i));

        System.out.printf("---PAGE %d OF %d---\n", currentIndex/pageSize + 1, (int) Math.ceil(size/pageSize));

        currentIndex = end;

    }

    public static void displayPrev() {


        System.out.println();

        if(currentIndex == 0) {
            System.out.println("No more pages.");
            return;
        }


        if(next) {
            currentIndex = currentIndex - pageSize >= 0? currentIndex - pageSize : 0;
            next = false;
        }

        if(currentIndex == 0) {
            System.out.println("No more pages.");
            return;
        }


        int begin = currentIndex - pageSize >= 0? currentIndex - pageSize : 0;

        for(int i = begin; i < currentIndex; i++)
            System.out.println(output.get(i));

        System.out.printf("---PAGE %d OF %d---\n", begin/pageSize + 1, (int) Math.ceil(size/pageSize));

        currentIndex = begin;
    }
}
