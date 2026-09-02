package com.isabela.testetecnicosea.model.document;

import com.isabela.testetecnicosea.model.enums.Priority;
import com.isabela.testetecnicosea.model.enums.ServiceType;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.model.enums.State;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "solicitations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitationDocument {

    @Id
    private String id;

    @Field(type = FieldType.Integer)
    private Integer clientId;

    @Field(type = FieldType.Keyword)
    private SolicitationStatus status;

    @Field(type = FieldType.Keyword)
    private ServiceType serviceType;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private State state;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private Priority priority;

    @Field(
            type = FieldType.Date,
            format = DateFormat.date_hour_minute_second_millis
    )
    private LocalDateTime createdAt;

    @Field(
            type = FieldType.Date,
            format = DateFormat.date_hour_minute_second_millis
    )
    private LocalDateTime submittedAt;

}
