package sample.meter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router
import sample.meter.MessageHandler


@Configuration
class RoutesConfig {
    
    @Bean
    fun apis(messageHandler: MessageHandler) = router {
        (accept(MediaType.APPLICATION_JSON) and "/messages").nest {
            POST("/", messageHandler::handleMessage)
        }
    }
    
}