import java.util.*;

public class Traffic {

    private static final Set<Account> ACCOUNTS = new HashSet<>(20);
    private static final Set<Car> CARS = new HashSet<>(20);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean again = true;
        while (again) {
            var task = scanner.nextLine();
            var t = task.split(" ");
            switch (t[0]) {
                case "REGISTER" -> register(t[1], t[2]);
                case "REGISTER_CAR" -> registerCar(t[1], t[2], t[3]);
                case "ADD_BALANCE" -> addBalance(t[1], Integer.parseInt(t[2]), t[3]);
                case "NEW_RECORD" -> newRecord(t[1], t[2]);
                case "BUY_LICENSE" -> buyLicense(t[1], t[2], Integer.parseInt(t[3]), t[4]);
                case "GET_PENALTY" -> getPenalty(t[1], t[2]);
                case "GET_BALANCE" -> getBalance(t[1], t[2]);
                case "GET_LICENSE_DEADLINE" -> licenseDedLine(t[1], t[2]);
                case "END" -> again = false;
            }
        }
    }

    private static final int PENALTY = 100;

    private static void newRecord(String plate, String time) {
        Car car = findByPlate(plate);
        if (car != null) {
            var t = new DateUtils.MyDate(time);
            int dow = DateUtils.isEvenOrOdd(t);
            if (dow == 2 || dow == car.code ||
                    car.licenses.stream().anyMatch(l -> {
                        var s = new DateUtils.MyDate(l.startDate);
                        var e = DateUtils.addDays(s, l.length);
                        return DateUtils.compareDates(t, s) >= 0 && DateUtils.compareDates(e, t) >= 0;
                    })) {
                System.out.println("NORMAL RECORDED");
            } else {
                System.out.println("PENALTY RECORDED");
                car.account.penalty += PENALTY;
            }
        } else System.out.println("INVALID CAR PLATE");
    }

    private static void licenseDedLine(String plate, String time) {
        Car car = findByPlate(plate);
        if (car != null) {
            var q = new DateUtils.MyDate(time);
            License valid = car.licenses.stream()
                    .filter(l -> {
                        var s = new DateUtils.MyDate(l.startDate);
                        var e = DateUtils.addDays(s, l.length);
                        return DateUtils.compareDates(q, s) >= 0 && DateUtils.compareDates(e, q) >= 0;
                    })
                    .findAny().orElse(null);
            if (valid != null)
                System.out.println(DateUtils.addDays(new DateUtils.MyDate(valid.startDate), valid.length + 1));
            else
                System.out.println(DateUtils.addDays(q, 1));
        } else System.out.println("INVALID CAR PLATE");
    }

    private static final int LICENSE_PER_DAY = 70;

    private static void buyLicense(String username, String plate, int l, String time) {
        Account a = findByUsername(username);
        if (a != null) {
            Car c = findByPlate(plate);
            if (c != null) {
                int price = l * LICENSE_PER_DAY;
                if (a.balance >= price) {
                    a.balance -= price;
                    c.licenses.add(new License(time, l));
                    System.out.println("BUY LICENSE DONE");
                } else System.out.println("NO ENOUGH MONEY");
            } else System.out.println("INVALID CAR PLATE");
        } else System.out.println("INVALID USERNAME");
    }

    private static void getPenalty(String username, String time) {
        Account a = findByUsername(username);
        if (a != null) {
            System.out.println(a.penalty);
        } else System.out.println("INVALID USERNAME");
    }

    private static void getBalance(String username, String time) {
        Account a = findByUsername(username);
        if (a != null) {
            System.out.println(a.balance);
        } else System.out.println("INVALID USERNAME");
    }

    private static void addBalance(String username, int amount, String time) {
        Account a = findByUsername(username);
        if (a != null) {
            a.balance += amount;
            System.out.println("ADD BALANCE DONE");
        } else System.out.println("INVALID USERNAME");
    }

    private static void registerCar(String username, String plate, String time) {
        if (plate.matches("^0?[0-9]{10}$") &&
                CARS.stream().noneMatch(car -> car.plate.equals(plate))) {
            Account a = findByUsername(username);
            if (a != null) {
                Car c = new Car(a, plate, time);
                CARS.add(c);
                System.out.println("REGISTER CAR DONE");
            } else System.out.println("INVALID USERNAME");
        } else System.out.println("INVALID CAR PLATE");
    }

    private static void register(String username, String time) {
        if (username.matches("^[a-zA-Z0-9]{1,20}$") &&
                ACCOUNTS.stream().noneMatch(account -> account.username.equals(username))) {
            ACCOUNTS.add(new Account(username, time));
            System.out.println("REGISTER DONE");
        } else System.out.println("INVALID USERNAME");
    }

    private static Account findByUsername(String u) {
        return ACCOUNTS.stream()
                .filter(account -> account.username.equals(u))
                .findAny().orElse(null);
    }

    private static Car findByPlate(String p) {
        return CARS.stream()
                .filter(car -> car.plate.equals(p))
                .findAny().orElse(null);
    }
}

class License {
    String startDate;
    int length;

    public License(String startDate, int length) {
        this.startDate = startDate;
        this.length = length;
    }
}

class Account {
    String username;
    String timestamp;
    int balance;
    long penalty;

    public Account(String username, String timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }
}

class Car {
    Account account;
    String plate;
    String timestamp;
    final List<License> licenses;
    int code;

    public Car(Account account, String plate, String timestamp) {
        this.account = account;
        this.plate = plate;
        this.timestamp = timestamp;
        licenses = new ArrayList<>();
        this.code = Character.getNumericValue(plate.charAt(9)) % 2;
    }
}

class DateUtils {

    public static class MyDate {
        int year, month, day;

        public MyDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }


        public MyDate(String str) {
            this.year = Integer.parseInt(str.substring(0, 4));
            this.month = Integer.parseInt(str.substring(5, 7));
            this.day = Integer.parseInt(str.substring(8));
        }

        @Override
        public String toString() {
            return year + "/" + (month < 10 ? "0" + month : month) + "/" + (day < 10 ? "0" + day : day);
        }

        public int toDays() {
            return (year - 1400) * 360 + (month - 1) * 30 + (day - 1);
        }

        public static MyDate fromDays(int totalDays) {
            int year = 1400 + (totalDays / 360);
            totalDays %= 360;
            int month = 1 + (totalDays / 30);
            totalDays %= 30;
            int day = 1 + totalDays;
            return new MyDate(year, month, day);
        }
    }

    public static int isEvenOrOdd(MyDate date) {
        int totalDays = date.toDays();
        int dayOfWeek = totalDays % 7;

        if (dayOfWeek == 6) {
            return 2;
        } else if (dayOfWeek == 0 || dayOfWeek == 2 || dayOfWeek == 4) {
            return 0;
        } else {
            return 1;
        }
    }

    public static MyDate addDays(MyDate date, int n) {
        int totalDays = date.toDays() + n;
        return MyDate.fromDays(totalDays);
    }

    public static int compareDates(MyDate date1, MyDate date2) {
        int days1 = date1.toDays();
        int days2 = date2.toDays();

        return Integer.compare(days1, days2);
    }
}