import React from 'react';

class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            "base" : {},
            "base name" : "base.json",
            "bases list" : [],
            "log" : []
        };
        this.handleForward = this.handleForward.bind(this);
        this.handleBaseChange = this.handleBaseChange.bind(this);
    }

    componentWillMount() {
        const url = "/api/bases";

        fetch(url, {
            method : 'GET'
        })
        .then(response => response.json())
        .then(data => {
            this.setState(data);
        })
        .catch(error => console.log(error));
    }

    handleForward() {
        document.getElementById("forward-button").disabled = true;

        let request = {
            "base" : this.state["base name"],
            "memory" : []
        };

        this.state["base"]["variables"].forEach(variable => {
            const name = variable["name"];
            const select = document.getElementById(name + "-select");
            const value = select.options[select.selectedIndex].value;
            request["memory"].push({
                "variable" : name,
                "value" : value
            });
        });

        const url = "/api/bases";

        fetch(url, {
            method : 'POST',
            body : JSON.stringify(request)
        })
        .then(response => response.json())
        .then(data => {
            let new_base = this.state.base;
            new_base.memory = data.memory;
            this.setState({
                log : data.log,
                base : new_base
            }, () => {
                document.getElementById("forward-button").disabled = false;
            });
        })
        .catch(error => console.log(error));
    }

    handleBaseChange() {
        const select = document.getElementById("base-select");
        const selectedBase = select.options[select.selectedIndex].value
        if (selectedBase !== "") {
            const url = "/api/bases?base=" + selectedBase;

            fetch(url, {
                method : 'GET'
            })
            .then(response => response.json())
            .then(data => {
                data["base name"] = selectedBase;
                data["log"] = [];
                this.setState(data);
            })
            .catch(error => console.log(error));
        }
    }

    render() {
        return (
            <div className="container">
                <div className="row">
                    <div className="col">
                        <KnowledgeBase rules={this.state.base["knowledge base"]} />
                    </div>
                </div>
                <div className="row">
                    <div className="col">
                        <Variables variables={this.state.base["variables"]} memory={this.state.base["memory"]} /> 
                    </div>
                </div>
                <div className="row">
                    <div className="col">
                        <Log log={this.state.log} />
                    </div>
                    <div className="col">
                        <Controls basesList={this.state["bases list"]}
                            onForward={this.handleForward}
                            onBaseChange={this.handleBaseChange} />
                    </div>
                </div>
            </div>
        );
    }
}

class Controls extends React.Component {
    constructor(props) {
        super(props);
        this.handleForward = this.handleForward.bind(this);
        this.handleBaseChange = this.handleBaseChange.bind(this);
    }
    handleForward() {
        this.props.onForward();
    }

    handleBaseChange() {
        this.props.onBaseChange();
    }

    render() {
        if (this.props.basesList) {
            const options = this.props.basesList.map((base) => {
                return (
                    <option value={base}>{base}</option>
                );
            });
            return (
                <div>
                    <button id="forward-button" onClick={this.handleForward}>Forward Chaining</button>
                    <select id="base-select" onChange={this.handleBaseChange}>
                        <option value="">-- Select a knowledge base --</option>
                        {options}          
                    </select>
                </div>
            );
        } else {
            return (
                <div></div>
            );
        }
    }
}

class Log extends React.Component {
    render() {
        return (
            <div>
                <textarea disabled={true} cols="40" rows="10" value={this.props.log.join("\n")}></textarea>
            </div>
        );
    }
}

class Variables extends React.Component {
    render() {
        if (this.props.variables) {
            const memory = groupBy(this.props.memory, "variable", "value");

            const variablesInputs = this.props.variables.map((variable) => {
                const name = variable["name"];
                const values = variable["values"];
                return (
                    <div>
                        <VariableInput name={name} values={values} selected={memory[name]}/>
                    </div>
                );
            });
            return (
                <div>
                    <h1>Variables</h1>
                    <div className="grid">
                        {variablesInputs}
                    </div>
                </div>
            );
        } else {
            return (
                <div>
                </div>
            );
        }
    }
}

class VariableInput extends React.Component {
    render() {
        if (this.props.values) {
            const options = this.props.values.map((value) => {
                return (
                    <option value={value} selected={this.props.selected === value}>{value}</option>
                );
            });
            return (
                <fieldset>
                    <legend>{this.props.name}</legend>
                    <select id={this.props.name + "-select"}>
                        <option value=""></option>
                        {options}
                    </select>
                </fieldset>
            );
        } else {
            return (
                <fieldset>
                </fieldset>
            );
        }

    }
}

class KnowledgeBase extends React.Component {
    render() {
        let rules = null;
        if (this.props.rules) {
            rules = this.props.rules.map((rule) => {
                const label = rule["label"];
                const antecedents = rule["antecedents"].map((clause) => {
                    return Object.values(clause).join(" ");
                }).join(" AND ");
                const consequent = Object.values(rule["consequent"]).join(" ");

                return (
                    <tr>
                        <td>{label}</td>
                        <td>{antecedents}</td>
                        <td>{consequent}</td>
                    </tr>
                );
            });
        }
        return (
            <div>
                <h1>Knowledge Base</h1>
                <table className="table table-striped table-hover table-bordered table-dark table-responsive align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>Label</th>
                            <th>Antecedents clauses</th>
                            <th>Consequent clause</th>
                        </tr>
                    </thead>
                    <tbody>
                        {this.props.rules ? rules : "No rules to display"}
                    </tbody>
                </table>
            </div>
        );
    }
}

function groupBy(list, key, value) {
    const map = {};
    list.forEach((item) => {
        const k = item[key];
        const v = item[value];
        map[k] = v;
    });
    return map;
}

export default App;
