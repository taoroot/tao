package cn.flizi.boot.tao;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.flizi.boot.tao.security.CustomAuthenticationEntryPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AppTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 未登录情况下
     * @see CustomAuthenticationEntryPoint
     * @throws Exception
     */
    @Test
    public void customAuthenticationEntryPoint() throws Exception {
        // @formatter:off
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        // @formatter:on
    }


    /**
     * 未登录情况下
     * @see CustomAuthenticationEntryPoint
     * @throws Exception
     */
    @Test
    public void customAccessDeniedHandler() throws Exception {
        // @formatter:off
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("username", "user");
        hashMap.put("password", "password");
        String content = new ObjectMapper().writeValueAsString(hashMap);
        this.mockMvc.perform(post("/login",  hashMap).contentType(MediaType.APPLICATION_JSON_VALUE).content(content)).andExpect(status().isOk());
        // @formatter:on
    }


}
