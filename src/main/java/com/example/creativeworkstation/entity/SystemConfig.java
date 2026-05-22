package com.example.creativeworkstation.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_system_config")
@Data
public class SystemConfig {
    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Column(name = "config_value")
    private String configValue;
}
