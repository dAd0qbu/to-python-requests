## To python requests
Another version of to-python-requests but it has a template file for generated code

Default template `template.py` is created at `%YOUR_BURP_PATH%/template.py` (The file path can be viewed in Extension output)

## Features
- `{% for %}` to loop through a list
- `{{var}}` to replace with a variable

## Example 
```python
import requests
{% for request in requests %}
def {{request.method}}{{request.safeRoute}}_{{index}}():
    r = requests.request(method="{{request.method}}", url="{{request.url}}", params="{{request.params}}", cookies={{request.cookies}}, {{request.dataParameterName}}={{request.data}})
    print(r.text)
{% %}
def main():
{% for request in requests %}    {{request.method}}{{request.safeRoute}}_{{index}}()
{% %}
main()

```

Request fields can be found in [`Request.java`](src/main/java/me/doqbu/Request.java)