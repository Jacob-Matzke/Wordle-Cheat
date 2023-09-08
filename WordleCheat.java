import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordleCheat {

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        Scanner fileRead = new Scanner(new File("wordle solutions.txt"));
        ArrayList<String> answers = new ArrayList<>();
        while (fileRead.hasNext())
            answers.add(fileRead.next());
        fileRead.close();

        Scanner userIn = new Scanner(System.in);
        Double[] percentages = initPercentages();
        ArrayList<ArrayList<Character>> allowEachLetter = initAllowList();
        ArrayList<ArrayList<Character>> denyEachLetter = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            denyEachLetter.add(new ArrayList<Character>());
        }
        StringBuilder greenTemplate = new StringBuilder(".....");

        System.out.println("What was your first guess? (The best option is salet)");
        String guess = userIn.nextLine();
        System.out.println("Was it correct?");
        boolean isSolved = userIn.nextLine().toLowerCase().startsWith("y") ? true : false;
        String lettersInWord = "";
        String goodLetters = "";
        String yellows = "";

        while (!isSolved) {
            // Pre Processing
            for (int i = 0; i < 5; i++)
                if (!lettersInWord.contains("" + guess.charAt(i)))
                    lettersInWord += guess.charAt(i);

            // Black Variables
            boolean[] blackIndeces = new boolean[5];
            for (int i = 0; i < 5; i++)
                blackIndeces[i] = true;

            // Green Processing
            System.out.println("Were there any greens?");
            boolean wereAnyGreens = userIn.nextLine().toLowerCase().startsWith("y") ? true : false;
            if (wereAnyGreens) {
                System.out.println("What letters were green?");
                StringBuilder greenIndeces = new StringBuilder(userIn.nextLine());
                boolean greenIsValid = false;
                while (!greenIsValid) {
                    try {
                        Integer.parseInt(greenIndeces.toString().replaceAll(" ", ""));
                        greenIsValid = true;
                    } catch (Exception E) {
                        System.out.println("INVALID INPUT What indexes were green?");
                        greenIndeces = new StringBuilder(userIn.nextLine());
                    }
                }

                for (String x : greenIndeces.toString().split(" ")) {
                    int index = Integer.parseInt(x) - 1;
                    blackIndeces[index] = false;
                    greenTemplate.setCharAt(index, guess.charAt(index));
                    char character = guess.charAt(index);
                    if (!goodLetters.contains("" + character))
                        goodLetters += character;
                }
            }

            // Yellow Processing
            System.out.println("Were there any yellows?");
            boolean wereAnyYellows = userIn.nextLine().toLowerCase().startsWith("y") ? true : false;
            if (wereAnyYellows) {
                System.out.println("What letters were yellow?");
                StringBuilder yellowIndeces = new StringBuilder(userIn.nextLine());
                boolean yellowIsValid = false;
                while (!yellowIsValid) {
                    try {
                        Integer.parseInt(yellowIndeces.toString().replaceAll(" ", ""));
                        yellowIsValid = true;
                    } catch (Exception E) {
                        System.out.println("INVALID INPUT What indexes were yellow?");
                        yellowIndeces = new StringBuilder(userIn.nextLine());
                    }
                }

                for (String x : yellowIndeces.toString().split(" ")) {
                    int index = Integer.parseInt(x) - 1;
                    blackIndeces[index] = false;
                    char character = guess.charAt(index);
                    if (allowEachLetter.get(index).contains(character))
                        allowEachLetter.get(index).remove(allowEachLetter.get(index).indexOf(character));
                    if (!denyEachLetter.get(index).contains(character))
                        denyEachLetter.get(index).add(character);
                    if (!goodLetters.contains("" + character))
                        goodLetters += character;
                    if (!yellows.contains("" + character))
                        yellows += character;
                }
            }

            // Black Processing
            for (int i = 0; i < 5; i++) {
                if (blackIndeces[i]) {
                    char character = guess.charAt(i);
                    for (int j = 0; j < 5; j++)
                        if (!denyEachLetter.get(j).contains(character) && !goodLetters.contains("" + character))
                            denyEachLetter.get(j).add(character);
                }
            }

            // Calculating Possible Words
            ArrayList<String> informationValidWords = new ArrayList<>();

            Pattern greenPattern = Pattern.compile(greenTemplate.toString());
            Matcher matcher = greenPattern.matcher("");
            for (int count = 0; count < answers.size();) {

                String currentWord = answers.get(count);
                String lettersUsed = "";
                matcher.reset(currentWord);
                boolean matchesGreenTemplate = matcher.matches();
                boolean matchesAllowList = true;
                boolean matchesDenyList = true;
                boolean hasUniqueLetters = true;
                boolean hasNoRepeats = true;
                int yellowCount = 0;

                for (int i = 0; i < 5; i++) {
                    char character = currentWord.charAt(i);

                    if (yellows.contains("" + character))
                        yellowCount++;
                    if (!allowEachLetter.get(i).contains(character))
                        matchesAllowList = false;
                    if (denyEachLetter.get(i).contains(character))
                        matchesDenyList = false;
                    if (lettersInWord.contains("" + character))
                        hasUniqueLetters = false;
                    if (lettersUsed.contains("" + character))
                        hasNoRepeats = false;
                    lettersUsed += character;

                }

                boolean isValid = matchesGreenTemplate && matchesAllowList && matchesDenyList
                        && (yellowCount >= yellows.length());
                if (isValid)
                    count++;
                else
                    answers.remove(count);

                boolean isValidUnique = hasUniqueLetters && hasNoRepeats;
                if (isValidUnique)
                    informationValidWords.add(currentWord);

            }

            // Calculating best guess

            percentages = calculatePercentages(answers);
            TreeMap<Double, String> rankingsAnswer = new TreeMap<>(Comparator.reverseOrder());

            for (String x : answers) {
                Double value = scoreWord(x, percentages);
                rankingsAnswer.put(value, x);
            }

            percentages = calculatePercentages(informationValidWords);
            TreeMap<Double, String> rankingsInfo = new TreeMap<>(Comparator.reverseOrder());

            for (String x : informationValidWords) {
                Double value = scoreWord(x, percentages);
                rankingsInfo.put(value, x);
            }
            ArrayList<String> top10 = new ArrayList<>();

            int count = 0;
            for (Entry<Double, String> e : rankingsAnswer.entrySet()) {
                if (count == 10)
                    break;
                top10.add((count + 1) + ": " + e.getValue() + "\t");
                count++;
            }
            for (; count < 10; count++)
                top10.add((count + 1) + ": n/a\t\t");
            System.out.println(top10);

            count = 0;
            for (Entry<Double, String> e : rankingsInfo.entrySet()) {
                if (count == 10)
                    break;
                top10.set(count, top10.get(count) + e.getValue());
                count++;
            }
            for (; count < 10; count++)
                top10.set(count, top10.get(count) + "n/a");

            System.out.println("     Best Guesses\n   Valid\tInfo");
            for (String x : top10)
                System.out.println(x);

            // Re-input
            System.out.println("What was your next guess?");
            guess = userIn.nextLine();
            System.out.println("Was it correct?");
            isSolved = userIn.nextLine().toLowerCase().startsWith("y") ? true : false;
        }

        userIn.close();
    }

    public static Double scoreWord(String x, Double[] weights) {
        Double sum = 0.0;
        for (int i = 0; i < 5; i++)
            sum += weights[x.charAt(i) - 97];
        return sum;
    }

    public static ArrayList<ArrayList<Character>> initAllowList() {
        ArrayList<ArrayList<Character>> allowEachLetter = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            allowEachLetter.add(new ArrayList<>(26));
            for (int j = 97; j < 123; j++)
                allowEachLetter.get(i).add((char) j);
        }
        return allowEachLetter;
    }

    public static Double[] initPercentages() {
        return new Double[] { 8.4452, 2.4253, 4.1143, 3.4041, 10.654, 1.9835, 2.6851, 3.3521, 5.8034, 0.2339, 1.819,
                6.2018, 2.7371, 4.9632, 6.5223, 3.1615, 0.2512, 7.7696, 5.7861, 6.3144, 4.0364, 1.3166, 1.6804, 0.3205,
                3.6726, 0.3465 };
    }

    public static Double[] calculatePercentages(ArrayList<String> possibleWords) {
        int[] totals = new int[26];
        int totalSum = possibleWords.size() * 5;

        for (String x : possibleWords)
            for (char y : x.toCharArray())
                totals[(int) y - 97]++;

        Double[] percentages = new Double[26];
        DecimalFormat df = new DecimalFormat(".####");

        for (int i = 0; i < 26; i++)
            percentages[i] = Double.parseDouble(df.format(totals[i] * 100.0 / totalSum));

        return percentages;
    }
}
