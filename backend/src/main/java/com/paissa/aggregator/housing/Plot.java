package com.paissa.aggregator.housing;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plots", uniqueConstraints = @UniqueConstraint(columnNames = {"ward_id", "plot_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @Column(name = "plot_number")
    private Integer plotNumber;

    @Convert(converter = PlotSizeConverter.class)
    private PlotSize size;

    private Long price;

    /** Raw PAISSADB purchase_system code (1-9); see {@link PurchaseSystem} for the normalized ownership category. */
    @Column(name = "purchase_system")
    private Integer purchaseSystem;

    private Integer lottoEntries;

    private Integer lottoPhase;

    private Long lottoPhaseUntil;

    private Double firstSeenTime;

    private Double lastUpdatedTime;
}
