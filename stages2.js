import http from 'k6/http';
import { check, sleep } from 'k6';

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
