# fairshareSim
### Simple Fair-sharing Simulator for Batch Systems
This tool emulates the behavior of classical fairsharing system that are used in PBS or Slurm resource managers.
It allows you to simulate and visualize various settings of fair sharing algorithm.

#### Main Features
The simulator works on the principle of replaying an existing workload, and it is compatible with the Standard Workload Format which is used in the popular Parallel Workloads Archive. Alternatively, the workload can be read either from a SQL database or even synthetically generated workloads can be used. The simulator operates on the principle of discrete time simulation, i.e., simulation progresses in discrete time steps that can be specified, e.g., a simulation tick may represent one minute, hour, day, etc. Based on the input workload, individual users of the system are identified and for each of them, a timeline is reconstructed by mimicking the original job submission and execution process. Based on this input, key metrics can then be easily plotted for each/selected user(s) and corresponding ticks. 

The simulator currently supports the computation and visualization of three metrics that are key to understanding the behavior of the fairshare algorithm. The first metric is the total cumulative resource consumption over time for a given user. The second metric is the current resource consumption (resource allocation). The third metric is then the Fairshare Factor (FF). The computation of FF values mimics the implementation available in PBS resource manager, but can be easily modified to mimic similar Slurm's metric. FF is of utmost importance because it depicts how the relative priority among users evolves over time, enabling system administrators to judge the impact of the chosen configuration of the fair-sharing setup.

The simulator contains several switches/parameters that can be used to easily modify the way each metric is calculated and therefore the way the final FF is generated. For example, the simulator supports different methods to calculate the amount of consumed resources, meaning that the usage measurment is either based solely on CPU time or some variant of Processor Equivalent is used instead. It also allows tracking the effect of the number of shares assigned to users (the number of shares represents the intended resource ratio between users). Last but not least it supports the simulation of the effect of different decay factors on the recorded overall usage and corresponding FF priorities. If the parameters of the nodes in the infrastructure are known, the simulator can be configured to take into account, for example, different speeds/prices of machines and thus weigh the consumed resources based on their speeds/prices.

The data sets in SWF format are available at https://jsspp.org/workload/ and http://www.cs.huji.ac.il/labs/parallel/workload/logs.html. Sample data set is provided within the distribution (see ./data-set directory) but only serve for demonstration purposes. 

##### Software licence:
This software is provided as is, free of charge under the terms of the LGPL licence. It uses jFreeChart library.

##### Important
When using fairhareSim in your paper or presentation, please use the following citations as an acknowledgement. Thank you!
- Dalibor Klusáček. Fair-Sharing Simulator for Batch Computing Systems. In proceedings of the 15th International Conference on Parallel Processing & Applied Mathematics (PPAM 2024), Springer, 2024.
