package com.aviation.reservation.config;

import com.aviation.reservation.entity.MonthlyStat;
import com.aviation.reservation.entity.Reservation;
import com.aviation.reservation.repository.MonthlyStatRepository;
import com.aviation.reservation.repository.ReservationRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final ReservationRepository reservationRepository;
    private final MonthlyStatRepository monthlyStatRepository;

    @Bean
    public Job monthlyStatJob(JobRepository jobRepository, Step monthlyStatStep) {
        return new JobBuilder("monthlyStatJob", jobRepository)
                .start(monthlyStatStep)
                .build();
    }

    @Bean
    public Step monthlyStatStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("monthlyStatStep", jobRepository)
                .<RouteStatRow, MonthlyStat>chunk(50, txManager)
                .reader(monthlyStatReader(null, null))
                .processor(monthlyStatProcessor())
                .writer(monthlyStatWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<RouteStatRow> monthlyStatReader(
            @Value("#{jobParameters['year']}") Integer year,
            @Value("#{jobParameters['month']}") Integer month) {

        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int targetYear  = (year  != null) ? year  : lastMonth.getYear();
        int targetMonth = (month != null) ? month : lastMonth.getMonthValue();

        List<Object[]> rows = reservationRepository.findMonthlyRouteStats(
                Reservation.ReservationStatus.CONFIRMED, targetYear, targetMonth);

        List<RouteStatRow> items = rows.stream()
                .map(r -> new RouteStatRow(
                        targetYear,
                        targetMonth,
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue(),
                        (BigDecimal) r[3]))
                .toList();

        log.info("[Batch] monthlyStatReader: {}-{} 기준 {} 노선 집계 시작",
                targetYear, targetMonth, items.size());
        return new ListItemReader<>(items);
    }

    @Bean
    public ItemProcessor<RouteStatRow, MonthlyStat> monthlyStatProcessor() {
        return row -> {
            MonthlyStat stat = monthlyStatRepository
                    .findByStatYearAndStatMonthAndOriginAndDestination(
                            row.getYear(), row.getMonth(), row.getOrigin(), row.getDestination())
                    .orElseGet(MonthlyStat::new);

            stat.setStatYear(row.getYear());
            stat.setStatMonth(row.getMonth());
            stat.setOrigin(row.getOrigin());
            stat.setDestination(row.getDestination());
            stat.setReservationCount(row.getCount());
            stat.setTotalRevenue(row.getRevenue());
            stat.setCalculatedAt(LocalDateTime.now());
            return stat;
        };
    }

    @Bean
    public ItemWriter<MonthlyStat> monthlyStatWriter() {
        return chunk -> {
            monthlyStatRepository.saveAll(chunk.getItems());
            log.info("[Batch] monthlyStatWriter: {} 건 저장 완료", chunk.getItems().size());
        };
    }

    @Getter
    @RequiredArgsConstructor
    public static class RouteStatRow {
        private final int year;
        private final int month;
        private final String origin;
        private final String destination;
        private final long count;
        private final BigDecimal revenue;
    }
}
