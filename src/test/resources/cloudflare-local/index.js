export default {
	async fetch(request, env) {
		return new Response("Hello world", {
			headers: { 'content-type': 'text/plain' },
		});
	},
};