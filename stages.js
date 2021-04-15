import http from 'k6/http';
import { check, sleep } from 'k6';


export let options = {
  discardResponseBodies: true,
  scenarios: {
    contacts: {
      executor: 'shared-iterations',
      vus: 100,
      iterations: 10000,
      maxDuration: '5m',
    },
  },
};

//export let options = {
//  max_vus: 1000,
//  vus: 100,
//  stages: [
//    { duration: '10s', target: 100 },
//    { duration: '2m', target: 1000 }
//  ]
//};

export default function () {
  const BASE_URL = 'http://localhost:8080'; // make sure this is not production
  let responses = http.batch([
    [
      'GET',
      `${BASE_URL}/hello`,
      null,
      { tags: { name: 'Customer register commands' } },
    ]
  ]);
   check(responses, {
      'is status 200': (r) => r.status === 200,
    });
  // sleep(1);
}
