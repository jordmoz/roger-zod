package com.moz.zod

import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.FrameworkInfo;

class Main {
	static void main(String [] args) {

		String mesosURL = "zk://daldevmesoszk01:2181,daldevmesoszk02:2181,daldevmesoszk03:2181/mesos"

		FrameworkInfo fi = FrameworkInfo.newBuilder()
				.setName("Zod")
				// .setHostname("dalstgmesos01")
				.setUser("root")
				.setRole("*")
				.setCheckpoint(false)
				.setFailoverTimeout(0.0d)
				.build()

		Zcheduler z = new Zcheduler();
		MesosSchedulerDriver driver = new MesosSchedulerDriver(z,
				fi,mesosURL, false)

		//run the driver
		driver.run()

	}
}
