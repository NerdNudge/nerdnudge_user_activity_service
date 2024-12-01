package com.neurospark.nerdnudge.useractivity;

import com.neurospark.nerdnudge.metrics.metrics.Metronome;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NerdnudgeUseractivityApplication {

	public static void main(String[] args) {
		Metronome.initiateMetrics(60000);
		SpringApplication.run(NerdnudgeUseractivityApplication.class, args);
	}

}
