package org.example;

import com.rabbitmq.client.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    private static final String PREFS = "prefs";

    public static void main(String[] args) throws Exception {


        ChromeOptions chrome_options = new ChromeOptions();
        chrome_options.addArguments("--disable-popup-blocking");
        chrome_options.addArguments("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0");
        chrome_options.addArguments("headless=True");
        chrome_options.addArguments("--disable-blink-features=AutomationControlled");
        chrome_options.addArguments("--window-size=1920,1080");
        chrome_options.addArguments("use_subprocess=True");

        Map<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("useAutomationExtension", false);
        chromePrefs.put("excludeSwitches", "enable-automation");
        chrome_options.setExperimentalOption(PREFS, chromePrefs);

        WebDriver driver = new ChromeDriver(chrome_options);
        driver.get("https://www.lme.com/en/Metals/Non-ferrous/LME-Copper");
        Thread.sleep(1000);
        String doc = driver.getPageSource();

        // В файл сохраняем просто так, особой необходимости в этом нет
        PrintWriter out = new PrintWriter("site.xml");
        out.print(doc);
        out.close();
        driver.quit();

        Element FirstTable = Jsoup.parse(doc).getElementsByTag("tbody").get(0).child(0);
        String dateExpiry = FirstTable.attr("data-table-row-hover").replace("Expiry date ", "");
        String value = (FirstTable.childNode(3).childNode(0)).toString();

        // Получаем месяц
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("MMM").withLocale(Locale.ENGLISH);
        String pattern = "[0-9]|\\s";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(dateExpiry);
        String month = m.replaceAll("");

        TemporalAccessor accessor = parser.parse(firstUpperCase(month.toLowerCase()));
        dateExpiry = dateExpiry.replace(month, accessor.get(ChronoField.MONTH_OF_YEAR) + "");  // prints 2

        System.out.println("DateExpiry " + dateExpiry);
        System.out.println("Value " + value);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("80.252.151.65");
        factory.setUsername("admin");
        factory.setPassword("v0776617");
        factory.setPort(5672);

        final String QUEUE_NAME = "LMEChanell";
        Map<String, Object> exchangeArguments = new HashMap<>();
        exchangeArguments.put("value", value);
        exchangeArguments.put("dateExpiry", dateExpiry);
        exchangeArguments.put("dateGetting", new java.util.Date());

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, true, false, false, exchangeArguments);

            String message = dateExpiry + " " + value;
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + dateExpiry + " " + value + "'");
        }

    }
    public static String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return ""; //или return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

/*
        apiToken = '6624169967:AAHPMBJuqoQnqP8djBsTE1fCx3QednoN6_k'
        chatID = '-1001937255547'
        apiURL = f'https://api.telegram.org/bot{apiToken}/sendMessage'
        try:
        response = requests.post(apiURL, json={'chat_id': chatID, 'text': "Курс меди: " + arrayOfStrings[2]})
        response = requests.post(apiURL, json={'chat_id': chatID, 'text': "Дата актуальности курса: " + temp.next.attrs['data-table-row-hover']})
        print(response.text)
        except Exception as e:
        print(e)

        now = datetime.now()
        message = now.strftime("%Y.%m.%d") + "; " + arrayOfStrings[2] + "; " + temp.next.attrs['data-table-row-hover']

# читаю последнюю строчку файла
        f_read = open('out.txt', "r")
        last_line = f_read.readlines()[-1]
        if last_line.find(message) == -1:
        file = open('out.txt', "a")
        file.write("\n")
        file.write(message)
        file.close()
*/




}