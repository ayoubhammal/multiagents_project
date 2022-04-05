function fetch_base(base = "base") {

    const url = "/api/bases?base=base";

    fetch(url, {
        method: 'GET', // *GET, POST, PUT, DELETE, etc.
        mode: 'cors', // no-cors, *cors, same-origin
    })
    .then(response => response.json())
    .then(data => print_base(data))
    .catch(error => console.log(error));
}

function print_base(data) {
    console.log(data);
}

let bases;

fetch_base();
