package com.moz.zod

import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.FrameworkInfo;

class Main {
	static void main(String [] args) {

		String mesosURL = "daldevmesoszk01:5050"

		FrameworkInfo fi = FrameworkInfo.newBuilder()
				.setName("Zod")
				.setHostname("10.0.5.126")
				.setUser("")
				.setRole("*")
				.setCheckpoint(false)
				.setFailoverTimeout(0.0d)
				.build()

		Zcheduler z = new Zcheduler();
		MesosSchedulerDriver driver = new MesosSchedulerDriver(z,
				fi,mesosURL)
		//run the driver
		driver.run()

	}
}
