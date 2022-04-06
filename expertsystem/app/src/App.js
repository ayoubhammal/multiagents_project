import './App.css';
import React from 'react';

class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    componentWillMount() {
    }

    render() {
        return (
            <div>
                <KnowledgeBase rules={this.state.base.rules}/>
            </div>
        );
    }
}

class KnowledgeBase extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        const rules = this.props.rules.map((rule) => {

            const label = rule["label"];
            const antecedents = rule["antecedents"].map((clause) => {
                return clause.values().join(" ");
            }).join(" AND ");
            const consequent = rule["consequent"].values().join(" ");
            return (
                <tr>
                    <td>{label}</td>
                    <td>{antecedents}</td>
                    <td>{consequent}</td>
                </tr>
            );
        });
        return (
            <table>
                <thead>
                    <tr>
                        <th>Label</th>
                        <th>Antecedents clauses</th>
                        <th>Consequent clause</th>
                    </tr>
                </thead>
                <tbody>
                    {rules}
                </tbody>
            </table>
        );
    }
}

export default App;
