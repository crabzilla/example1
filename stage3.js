import http from 'k6/http';


export let options = {
  scenarios: {
    constant_request_rate: {
      executor: 'constant-arrival-rate',
      rate: 1000,
      timeUnit: '1s',
      duration: '15s',
      preAllocatedVUs: 100,
      maxVUs: 200,
    },
  },
};


export default function () {
  const payload = JSON.stringify({ correlationId: `${__VU}${__ITER}-${__VU}${__ITER}`, name: `name${__VU}${__ITER}`, email: `user${__VU}${__ITER}@mail.com`, password: "123456" });
  const params = { headers: { 'Content-Type': 'application/json' } };
  // let r = http.post('http://localhost:18081/identity/api/v1/user' , payload, params);
  let r = http.post('http://localhost:8080/hello2');
};

