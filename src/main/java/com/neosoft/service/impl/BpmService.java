package com.neosoft.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.form.BooleanFormType;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.neosoft.beans.UserBean;
import com.neosoft.service.IBpmService;

@Service
public class BpmService implements IBpmService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BpmService.class);

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired 
	private FormService formService;

	@Autowired
	private HistoryService historyService;

	@Value("${process.instance.key}")
	private String processInstanceKey;

	public void onboard(UserBean userBean) {
		ProcessInstance processInstance = this.runtimeService.startProcessInstanceByKey(this.processInstanceKey);
		LOGGER.info("Process Instance :: {}", processInstance);
		LOGGER.info("Process Instance :: {}", processInstance.getProcessInstanceId());
		LOGGER.info("Process Instance :: {}", processInstance.getProcessVariables());

		while (processInstance != null && !processInstance.isEnded()) {
			List<Task> taskList = this.taskService.createTaskQuery().taskCandidateGroup("managers").list();
			//LOGGER.info("Task List :: {}", taskList);

			if(taskList != null && !taskList.isEmpty() && taskList.size() > 0) {
				for(Task task : taskList) {
					LOGGER.info("Task :: {}", task);
					
					Map<String, Object> variables = new HashMap<>();
					FormData formData = this.formService.getTaskFormData(task.getId());
					for (FormProperty formProperty : formData.getFormProperties()) {
						if (StringFormType.class.isInstance(formProperty.getType())) {
							variables.put(formProperty.getId(), userBean.getName());
						} else if (LongFormType.class.isInstance(formProperty.getType())) {
							variables.put(formProperty.getId(), userBean.getAge());
						} else if (BooleanFormType.class.isInstance(formProperty.getType())) {
							variables.put(formProperty.getId(), userBean.isUserAdmin());
						}
					}
					this.taskService.complete(task.getId(), variables);
					
					HistoricActivityInstance historicActivityInstance = null;
					List<HistoricActivityInstance> histricActivityInstanceList = this.historyService.createHistoricActivityInstanceQuery()
																				   .processInstanceId(processInstance.getId())
																				   .finished()
																				   .orderByHistoricActivityInstanceEndTime()
																				   .asc()
																				   .list();
					
					for (HistoricActivityInstance historicActivityInstanceInner : histricActivityInstanceList) {
						if (historicActivityInstanceInner.getActivityType().equals("Start")) {
							LOGGER.info("Activity Start Time :: {}", historicActivityInstanceInner.getStartTime());
						}
						if (historicActivityInstanceInner.getActivityType().equals("End")) {
							historicActivityInstance = historicActivityInstanceInner;
						} else {
							LOGGER.info("Activity Details :: {} {} {}", historicActivityInstanceInner.getActivityName(), historicActivityInstanceInner.getActivityId(), historicActivityInstanceInner.getDurationInMillis());
						}
					}
					
					if (historicActivityInstance != null) {
						LOGGER.info("Activity Details :: {} {} {}", historicActivityInstance.getActivityName(), historicActivityInstance.getActivityId(), historicActivityInstance.getDurationInMillis());
						LOGGER.info("Activity Completion Details :: {} {} ", processInstance.getProcessDefinitionKey(), historicActivityInstance.getEndTime());
					}
					
				}
				processInstance = this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
			}
		}
	}
}
