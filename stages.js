import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  max_vus: 1000,
  vus: 500,
  stages: [
    { duration: '30s', target: 5 },
    { duration: '1m', target: 100 },
    { duration: '2m', target: 500 },
    { duration: '30s', target: 5 }
  ]
};

export default function () {
  let res = http.get('http://localhost:8080/hello1');
  check(res, { 'status was 200': (r) => r.status == 200 });
  sleep(1);
}
