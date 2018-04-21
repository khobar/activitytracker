import {Type, Types} from "../../models/type";

export const chartsOptions = {
  reportChartType: 'bar',

  distChartType:'doughnut',
  distChartLabels: [Types[Type.DEV], Types[Type.SM], Types[Type.NON_WORKING]],
  reportColors: [
    {
      backgroundColor: 'rgba(44,90,160,1)',
      borderColor: 'rgba(220,220,220,1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(220,220,220,1)',
      pointBorderColor: '#294284',
      pointHoverBackgroundColor: '#294284',
      pointHoverBorderColor: 'rgba(220,220,220,1)',
    },
    {
      backgroundColor: 'rgba(40,160,89,1)',
      borderColor: 'rgba(151,187,205,1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(151,187,205,1)',
      pointBorderColor: '#2a6f3c',
      pointHoverBackgroundColor: '#2a6f3c',
      pointHoverBorderColor: 'rgba(151,187,205,1)',
    }
  ],
  distChartColors: [{
    hoverBorderColor: ['rgba(0, 0, 0, 0.1)', 'rgba(0, 0, 0, 0.1)'],
    hoverBorderWidth: 0,
    backgroundColor: ["#2c5aa0", "#28a059"],
    hoverBackgroundColor: ["#2d72b9", "#28a059"]
  }],
  reportOptions: {
    responsive: true,
    scales: {
      yAxes: [{
        stacked: true
      }]
    }
  },
  distOptions: {
    responsive: false
  }
};
