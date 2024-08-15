import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class FileSimilarity {
    public static Map<String, List<Long>> fileFingerprints = new HashMap<>();

    public static class FileSumRunnable implements Runnable {
        private String path;

        public FileSumRunnable(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            try {
                List<Long> fingerprint = fileSum(path);
                fileFingerprints.put(path, fingerprint);
            } catch(Exception e) {}
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        // Create a map to store the fingerprint for each fil
        Thread[] threads = new Thread[args.length];

        int index = -1;

        // Calculate the fingerprint for each file
        for (String path : args) {
            index++;
            FileSumRunnable run = new FileSumRunnable(path);
            Thread newThread = new Thread(run);
            threads[index] = newThread;
            newThread.run();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Compare each pair of files
        for (int i = 0; i < args.length; i++) {
            for (int j = i + 1; j < args.length; j++) {
                String file1 = args[i];
                String file2 = args[j];
                List<Long> fingerprint1 = fileFingerprints.get(file1);
                List<Long> fingerprint2 = fileFingerprints.get(file2);
                float similarityScore = similarity(fingerprint1, fingerprint2);
                System.out.println("Similarity between " + file1 + " and " + file2 + ": " + (similarityScore * 100) + "%");
            }
        }
    }

    private static List<Long> fileSum(String filePath) throws IOException {
        File file = new File(filePath);
        List<Long> chunks = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[100];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                long sum = sum(buffer, bytesRead);
                chunks.add(sum);
            }
        }
        return chunks;
    }

    private static long sum(byte[] buffer, int length) {
        long sum = 0;

        try {
            for (int i = 0; i < length; i++) {
                sum += Byte.toUnsignedInt(buffer[i]);
            }
        } catch (Exception e) {}

        return sum;
    }

    private static float similarity(List<Long> base, List<Long> target) {
        int counter = 0;
        List<Long> targetCopy = new ArrayList<>(target);

        for (Long value : base) {
            if (targetCopy.contains(value)) {
                counter++;
                targetCopy.remove(value);
            }
        }

        return (float) counter / base.size();
    }
}
