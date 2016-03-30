package com.moz.zod

import java.util.logging.Level
import java.util.logging.Logger

import org.apache.mesos.Protos
import org.apache.mesos.Scheduler
import org.apache.mesos.SchedulerDriver
import org.apache.mesos.Protos.ExecutorID
import org.apache.mesos.Protos.FrameworkID
import org.apache.mesos.Protos.MasterInfo
import org.apache.mesos.Protos.Offer
import org.apache.mesos.Protos.OfferID
import org.apache.mesos.Protos.SlaveID
import org.apache.mesos.Protos.Status
import org.apache.mesos.Protos.TaskStatus

import com.google.common.collect.ImmutableList;

class Zcheduler implements Scheduler {
	static Logger logger = Logger.getLogger(Zcheduler.getName())
	int scheduled = 0;

	@Override
	public void registered(SchedulerDriver driver, FrameworkID frameworkId, MasterInfo masterInfo) {
		logger.log(Level.INFO, "registered: frameworkId: ${frameworkId}, masterInfo: ${masterInfo}.")
	}

	@Override
	public void reregistered(SchedulerDriver driver, MasterInfo masterInfo) {
		logger.log(Level.INFO, "reregistered: masterInfo: ${masterInfo}.")
	}

	@Override
	public void resourceOffers(SchedulerDriver driver, List<Offer> offers) {

		if(scheduled > 0) {
			offers.each { offer ->
				driver.declineOffer(offer.getId())
			}
		}
		else {
			Offer offer = offers.get(0);
			List<Protos.TaskInfo> tasks = new ArrayList<Protos.TaskInfo>();

			// generate a unique task ID
			Protos.TaskID taskId = Protos.TaskID.newBuilder()
					.setValue(UUID.randomUUID().toString()).build();

			// docker parameter info
			Protos.Parameter p0 = Protos.Parameter.newBuilder().setKey("user").setValue("31337").build();
			List<Protos.Parameter> parameters = ImmutableList.of(p0);

			// docker image info
			Protos.ContainerInfo.DockerInfo.Builder dockerInfoBuilder = Protos.ContainerInfo.DockerInfo.newBuilder();
			dockerInfoBuilder.setImage("ubuntu:latest");
			dockerInfoBuilder.addAllParameters(parameters);
			dockerInfoBuilder.setNetwork(Protos.ContainerInfo.DockerInfo.Network.BRIDGE);


			// container info
			Protos.ContainerInfo.Builder containerInfoBuilder = Protos.ContainerInfo.newBuilder();
			containerInfoBuilder.setType(Protos.ContainerInfo.Type.DOCKER);
			containerInfoBuilder.setDocker(dockerInfoBuilder.build());

			// create task to run
			Protos.TaskInfo task = Protos.TaskInfo.newBuilder()
					.setName("task-" + taskId.getValue())
					.setTaskId(taskId)
					.setSlaveId(offer.getSlaveId())
					.addResources(Protos.Resource.newBuilder()
					.setName("cpus")
					.setType(Protos.Value.Type.SCALAR)
					.setScalar(Protos.Value.Scalar.newBuilder().setValue(1)))
					.addResources(Protos.Resource.newBuilder()
					.setName("mem")
					.setType(Protos.Value.Type.SCALAR)
					.setScalar(Protos.Value.Scalar.newBuilder().setValue(128)))
					.setContainer(containerInfoBuilder)
					.setCommand(
					Protos.CommandInfo.newBuilder().setShell(false)
					.addArguments("/usr/bin/id"))
					.build();

			tasks.add(task);

			Protos.Filters filters = Protos.Filters.newBuilder().setRefuseSeconds(1).build();
			Status status = driver.launchTasks(ImmutableList.of(offer.getId()), tasks, filters);
			println "Launch status: ${status}"
			scheduled++
		}
	}


	@Override
	public void offerRescinded(SchedulerDriver driver, OfferID offerId) {
	}

	@Override
	public void statusUpdate(SchedulerDriver driver, TaskStatus status) {
		logger.log(Level.INFO, "statusUpdate: status: ${status}")
		driver.acknowledgeStatusUpdate(status);
	}

	@Override
	public void frameworkMessage(SchedulerDriver driver, ExecutorID executorId, SlaveID slaveId, byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnected(SchedulerDriver driver) {
		logger.log(Level.WARNING, "disconnected")
	}

	@Override
	public void slaveLost(SchedulerDriver driver, SlaveID slaveId) {
		logger.log(Level.WARNING, "slaveLost: slaveId: ${slaveId}")
	}

	@Override
	public void executorLost(SchedulerDriver driver, ExecutorID executorId, SlaveID slaveId, int status) {
		logger.log(Level.WARNING, "executorLost: executor ${executorId}, slaveId: ${slaveId}, status: ${status}")
	}

	@Override
	public void error(SchedulerDriver driver, String message) {
		logger.log(Level.SEVERE, message)
	}
}
