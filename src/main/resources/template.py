import requests

{% for request in requests %}
def {{request.method}}{{request.safeRoute}}_{{index}}():
    r = requests.request(method="{{request.method}}", url="{{request.url}}", params="{{request.params}}", cookies={{request.cookies}}, {{request.dataParameterName}}={{request.data}})
    r = r.text
{% %}