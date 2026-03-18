package com.perscholas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * JPA entity mapped to the {@code productlines} table.
 */
@Entity
@Table(name = "productlines")
@Getter @Setter @NoArgsConstructor
@ToString(exclude = "products")
public class ProductLine {

    @Id
    @Column(name = "productLine", length = 50)
    private String productLine;

    @Column(length = 4000)
    private String textDescription;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String htmlDescription;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] image;

    @OneToMany(mappedBy = "productLine", fetch = FetchType.LAZY)
    private List<Product> products;
}
