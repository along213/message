package com.example.demo.small_talk.Repository;

import com.example.demo.Bean.DO.DocumentMsg;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends ElasticsearchRepository<DocumentMsg,String> {

    @NonNull
    Optional<List<DocumentMsg>> findByMsg(@NonNull String id);

}
