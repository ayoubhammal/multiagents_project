function fetch_base(base = "base.json") {

    const url = "/api/bases?base=" + base;

    fetch(url, {
        method: 'GET', // *GET, POST, PUT, DELETE, etc.
        mode: 'cors', // no-cors, *cors, same-origin
    })
    .then(response => response.json())
    .then(data => print_base(data))
    .catch(error => console.log(error));
}

function print_base(data) {
    const variables_list = document.getElementById("variables-list");
    variables_list.innerHTML = "";
    const knowledgebase_body = document.getElementById("knowledgebase-body");
    knowledgebase_body.innerHTML = "";
    const bases_select = document.getElementById("bases-select");
    bases_select.innerHTML = "";

    // variables
    variables_selects = {};
    const variables = data["base"]["variables"];
    variables.forEach(variable => {
        const name = variable["name"];
        const values = variable["values"];

        const li = document.createElement("li");

        const select = document.createElement("select");
        variables_selects[name] = select;

        const option = document.createElement("option");
        option.value = "";
        option.appendChild(document.createTextNode(""));
        select.appendChild(option);

        values.forEach(value => {
            const option = document.createElement("option");
            option.value = value;

            option.appendChild(document.createTextNode(value));

            select.appendChild(option);
        });
        
        li.appendChild(document.createTextNode(name));
        li.appendChild(select);

        variables_list.appendChild(li);
    });

    // memory
    const memory = data["base"]["memory"];
    memory.forEach(variable => {
        const name = variable["variable"];
        const value = variable["value"];

        const option_index = [...variables_selects[name].options].map(o => o.text).indexOf(value);
        
        variables_selects[name].selectedIndex = option_index;
    });

    // knowledge base
    const rules = data["base"]["knowledge base"];
    rules.forEach(rule => {
        const label = rule["label"];
        const antecedents = rule["antecedents"];
        const consequent = rule["consequent"];

        let antecedents_str = [];
        antecedents.forEach(clause => {
            antecedents_str.push(clause["variable"] + " " + clause["condition"] + " " + clause["value"]);
        });
        antecedents_str = antecedents_str.join(" ");

        let consequent_str = consequent["variable"] + " " + consequent["condition"] + " " + consequent["value"];

        const tr = document.createElement("tr");
        const td_label = document.createElement("td");
        td_label.appendChild(document.createTextNode(label));
        const td_antecedents = document.createElement("td");
        td_antecedents.appendChild(document.createTextNode(antecedents_str));
        const td_consequent = document.createElement("td");
        td_consequent.appendChild(document.createTextNode(consequent_str));

        tr.appendChild(td_label);
        tr.appendChild(td_antecedents);
        tr.appendChild(td_consequent);

        knowledgebase_body.appendChild(tr);
    });

    // bases
    const bases = data["bases list"]; 
    const option = document.createElement("option");
    option.value = "";
    option.appendChild(document.createTextNode("--choose a base--"));
    bases_select.appendChild(option);
    bases.forEach(base => {
        const option = document.createElement("option");
        option.value = base;
        option.appendChild(document.createTextNode(base));

        bases_select.appendChild(option);
    });
}

function fetch_result() {

}

let variables_selects = null;
fetch_base();

// mounting event handlers
const bases_select = document.getElementById("bases-select");
bases_select.onchange = function (e) {
    const selected_option = e.target.options[e.target.selectedIndex].value;
    fetch_base(selected_option);
}
const forword_button = document.getElementById("forward-button");
forword_button.onclick = function (e) {
    const body = {};
    
    variables_selects.forEach(variable => {
        body[variable
    });
}
