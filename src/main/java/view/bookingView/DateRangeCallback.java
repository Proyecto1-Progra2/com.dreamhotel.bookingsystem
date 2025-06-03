package view.bookingView;

import java.time.LocalDate;

@FunctionalInterface
public interface DateRangeCallback {
    void onDateRangeSelected(LocalDate startDate, LocalDate endDate);
}
