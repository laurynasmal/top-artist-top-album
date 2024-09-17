package org.example.topartisttopalbum;

import org.example.topartisttopalbum.scheduling.ItunesApiCallResetTask;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@MockBean(ItunesApiCallResetTask.class)
class TopArtistTopAlbumApplicationTests {


    @Test
    void contextLoads() {
    }

}
