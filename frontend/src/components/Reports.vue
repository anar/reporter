<template>
<div>
	<h2>Reports <small>{{reports.length}}</small></h2>
	
	<transition-group class="list-group" name="list" tag="ul">
			<li class="list-group-item" v-for="(report, index) in reports" :key="report">  
				<div class="row">
					<div class="col-sm-5">Id: {{ report.id }}</div>
					<div class="col-sm-5">Type: {{ report.payload.reportType }}</div>
					<div class="col-sm-2">	
						<button v-if="report.state === 'BLOCKED'" type="button" class="btn btn-secondary btn-sm" disabled>Blocked</button>
						<button v-if="report.state !== 'BLOCKED'" type="button" class="btn btn-secondary btn-sm" v-on:click="block(report.id, index)">Block</button>
					</div>
				</div>
				<div class="row">
					<div class="col-sm-5">
						State: {{ report.state }}<br>
						<a href="#">Details</a>
					</div>
					<div class="col-sm-5">Message: {{ report.payload.message }}</div>
					<div class="col-sm-2">
						<button type="button" class="btn btn-primary btn-sm" v-on:click="resolve(report.id, index)">Resolve</button>
					</div>
				</div>
			</li>
	</transition-group>
	<br>
	<button type="button" class="btn btn-primary btn-sm" v-on:click="getReports()">Load more</button>
	<span v-if="loading">Loading...</span>
</div>
</template>

<script>

import Vue from 'vue'
import axios from 'axios'
Vue.use(axios)


export default {
  name: 'Reports',
  data () {
	  return {
        loading:false,
        reports:[],
   		nextOffset: '',
   		size: 11
	  }
  },  
  mounted () {
		this.getReports();
	},
	
 methods: {
	getReports: function(){ 
		
		this.loading = true;
		axios		
			.get('/api/reports', {params: {'nextOffset' : this.nextOffset, 'size' : this.size}})
			.then(response => {
				this.size = response.data.size;			
				this.reports = this.reports.concat(response.data.elements);
	        	this.nextOffset = response.data.nextOffset;
	      })
	      .catch(error => {
	        console.log(error)	    
	      })
	      .finally(() => {
	      	this.loading = false;
	      });
	     	      
	},
	block: function(id, index){ 
		
		var _event = event; // to access inside finally method 
		_event.target.disabled = true;
		
		axios
	      .put('/api/reports/'+id,{"ticketState": "BLOCKED"})
	      .then(response => {
	        Vue.set(this.reports, index, response.data);	        
	      })
	      .catch(error => {
	    	  alert('Error blocking the report!');
	    	  console.log(error);
	      })
		.finally(() => _event.target.disabled = false)
	      
	},
	resolve: function(id, index){
		
		var _event = event; // to access inside finally method 
		_event.target.disabled = true;
		
		axios
	      .put('/api/reports/'+id,{"ticketState": "CLOSED"})
	      .then(response => {
	    	  this.reports.splice(index, 1)
	      })
	      .catch(error => {
	    	  alert('Error resolving the report!');
	    	  console.log(error);	     
	      })
	      .finally(() => _event.target.disabled = false)
	}
}
}
</script>

<style scoped>
.list-leave-active {
  transition: all .5s;
}
.list-enter, .list-leave-to /* .list-leave-active below version 2.1.8 */ {
  opacity: 0;
  transform: translateX(200px);
}
</style>
