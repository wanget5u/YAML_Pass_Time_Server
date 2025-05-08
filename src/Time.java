import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Locale;

public class Time
{
    public static String passed(String date1, String date2)
    {
        try
        {
            boolean withTime = date1.contains("T") && date2.contains("T");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd['T'HH:mm]").withResolverStyle(ResolverStyle.STRICT);
            ZoneId zone = ZoneId.of("Europe/Warsaw");

            Temporal date1Temporal, date2Temporal;
            ZonedDateTime zonedDateTime1, zonedDateTime2;

            if (withTime)
            {
                LocalDateTime localDate1Time = LocalDateTime.parse(date1, dateTimeFormatter), localDate2Time = LocalDateTime.parse(date2, dateTimeFormatter);
                zonedDateTime1 = localDate1Time.atZone(zone); zonedDateTime2 = localDate2Time.atZone(zone);
            }
            else
            {
                LocalDate localDate1 = LocalDate.parse(date1, dateTimeFormatter), localDate2 = LocalDate.parse(date2, dateTimeFormatter);
                zonedDateTime1 = localDate1.atStartOfDay(zone);  zonedDateTime2 = localDate2.atStartOfDay(zone);
            }

            date1Temporal = zonedDateTime1;
            date2Temporal = zonedDateTime2;

            long daysBetween = ChronoUnit.DAYS.between(zonedDateTime1.toLocalDate(), zonedDateTime2.toLocalDate());
            double weeksBetween = Math.round((daysBetween / 7.0) * 100) / 100.0;

            String date1Message, date2Message;

            if (withTime)
            {
                LocalDateTime localDate1Time = zonedDateTime1.toLocalDateTime(), localDate2Time = zonedDateTime2.toLocalDateTime();
                dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu (EEEE) 'godz.' HH:mm", new Locale("PL")).withResolverStyle(ResolverStyle.STRICT);

                date1Message = localDate1Time.format(dateTimeFormatter); date2Message = localDate2Time.format(dateTimeFormatter);
            }
            else
            {
                LocalDate localDate1 = zonedDateTime1.toLocalDate(), localDate2 = zonedDateTime2.toLocalDate();
                dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu (EEEE)", new Locale("PL")).withResolverStyle(ResolverStyle.STRICT);

                date1Message = localDate1.format(dateTimeFormatter); date2Message = localDate2.format(dateTimeFormatter);
            }

            String returnMessage =
                    "Od " + date1Message + " do " + date2Message + "\n" +
                            " - mija: " + daysBetween + " " + variety(daysBetween, "dzień", "dni", "dni") + ", " + variety((long) weeksBetween, "tydzień", "tygodnie", "tygodni") + " " + weeksBetween + "\n";

            if (withTime)
            {
                long minutesBetween = ChronoUnit.MINUTES.between(date1Temporal, date2Temporal);
                long hoursBetween = minutesBetween / 60;
                returnMessage += " - " + variety(hoursBetween, "godzina", "godziny", "godzin") + ": " + hoursBetween + ", " + variety(minutesBetween, "minuta", "minuty", "minut") + ": " + minutesBetween + "\n";
            }

            returnMessage += " - kalendarzowo: ";

            if (daysBetween > 0)
            {
                Period period = Period.between(zonedDateTime1.toLocalDate(), zonedDateTime2.toLocalDate());

                boolean withComma = false;

                if (period.getYears() > 0)
                {
                    returnMessage += period.getYears() + " " + variety(period.getYears(), "rok", "lata", "lat");
                    withComma = true;
                }
                if (period.getMonths() > 0)
                {
                    if (withComma) returnMessage += ", ";
                    returnMessage += period.getMonths() + " " + variety(period.getMonths(), "miesiąc", "miesiące", "miesięcy");
                    withComma = true;
                }
                if (period.getDays() > 0)
                {
                    if (withComma) returnMessage += ", ";
                    returnMessage += period.getDays() + " " + variety(period.getDays(), "dzień", "dni", "dni");
                }
            }

            return returnMessage;
        }
        catch (DateTimeParseException exception)
        {return "*** " + exception;}
    }

    public static String variety(long amount, String singular, String few, String many)
    {
        if (amount == 1) return singular;
        long unitNumber = amount % 10;
        long tensNumber = amount % 100;
        if (2 <= unitNumber && unitNumber <= 4 && !(12 <= tensNumber && tensNumber <= 14)) return few;
        return many;
    }
}