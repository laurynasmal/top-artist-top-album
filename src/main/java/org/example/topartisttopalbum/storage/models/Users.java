package org.example.topartisttopalbum.storage.models;

//import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class Users {

    @Id
    private Long id;
    private String userId;

    @Column("artist_id")
    private Long artistId;
}
